package ru.tikhonovdo.enrichment.service.importscenario.alfabank

import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.Selenide.webdriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.DataType
import ru.tikhonovdo.enrichment.service.file.RawDataService
import ru.tikhonovdo.enrichment.service.importscenario.*
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Function
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.random.Random

@Component
class AlfabankImportScenario(
    @Value("\${selenoid-host-download-path}") private val hostDownloadPath: String,
    @Value("\${import.alfa.home-url}") private val homeUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,

    private val rawDataService: RawDataService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
) : AbstractImportScenario(context, waitingDuration, Bank.ALFA) {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    init {
        stepActions = mapOf(
            INITIAL.stepName to Function { requestOtpCode(it) },
            OTP_SENT.stepName to Function {
                val newState = finishLogin(it)
                switchState(newState)
                performImportStep(newState.stepName, it)
            },
            LOGIN_SUCCEED.stepName to Function { saveData() }
        )
    }

    fun requestOtpCode(scenarioData: ImportScenarioData): ScenarioState {
        resetDownloadPath()
        runRequestOtpCodeScenario(scenarioData)

        val driver = webdriver().`object`()

        screenshot("alfa-04", driver)
        return if (elementPresented("//input[@autocomplete='one-time-code']", driver)) {
            OTP_SENT
        } else {
            INITIAL
        }
    }

    private fun resetDownloadPath() {
        val path = Path(hostDownloadPath)
        FileSystemUtils.deleteRecursively(path)
        Files.createDirectories(path)
    }

    private fun runRequestOtpCodeScenario(scenarioData: ImportScenarioData) {
        val random = Random(System.currentTimeMillis())

        open(homeUrl)
        val driver = webdriver().`object`()
        random.sleep(2000, 5000)

        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("alfa-01", driver)

        wait.untilAppears("//input[@type='text']").sendKeys(scenarioData.login)
        wait.untilAppears("//input[@type='password']").sendKeys(scenarioData.password)
        screenshot("alfa-02", driver)
        random.sleep(2500, 4000)

        wait.untilAppears("//button[@type='submit']").click()
        screenshot("alfa-03", driver)
        random.sleep(1000, 1500) // wait for request complete
    }

    fun finishLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("alfa-11")

        scenarioData.otpCode!!
            .map { it.toString() }
            .forEachIndexed { index: Int, char: String ->
                wait.untilAppears("(//input)[${index + 1}]").sendKeys(char)
                random.sleep(80, 150)
            }
        screenshot("alfa-12")
        Thread.sleep(1000) // simulate navigation

        wait.untilAppears("//*[@data-test-id='trust-device-page-cancel-btn']").click()
        screenshot("alfa-13")

        return if (elementPresented("//*[@data-test-id='main-menu-layout-header-root']")) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    // todo: провести ресерч на возможность непосредственного использования API и отказа от работы файлами
    fun saveData(): ScenarioState {
        val downloadPath = Path(hostDownloadPath)

        log.info("Starting save data scenario")
        saveDataScenario()

        try {
            val report = getDownloadedFile(downloadPath)
            rawDataService.saveData(DataType.ALFA, content = arrayOf(report.readBytes()))
            FileSystemUtils.deleteRecursively(downloadPath)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            return FAILURE
        }
        return DATA_SAVED
    }

    private fun saveDataScenario() {
        val random = Random(System.currentTimeMillis())
        val driver = driver()
        val wait = WebDriverWait(driver, waitingDuration)

        log.trace("Open history page")
        driver.get(URI.create(driver.currentUrl).resolve("history").toString())
        screenshot("alfa-21")
        random.sleep(1000, 1500) // wait for the next form

        val operationsReportLink = wait.untilAppears("//*[@data-test-id='categories-group']/../button")
        random.sleep(1000, 2000) // simulate pointing on link

        log.trace("Click on operations report link")
        operationsReportLink.click()
        screenshot("alfa-22")

        log.trace("Click on 6 months period")
        wait.untilAppears("(//*[@data-test-id='quick-period-tags']//following-sibling::button)[last()]")
            .click()
        screenshot("alfa-23")
        random.sleep(500, 1000)

        val accountSelectField = wait.untilAppears("//*[@data-test-id='account-select-field']")
        random.sleep(1000, 2000) // simulate pointing on link

        log.trace("Show select accounts menu")
        accountSelectField.click()
        screenshot("alfa-24")

        wait.untilAppears("//*[@data-test-id='account-select-option']").click()
        screenshot("alfa-25")
        log.trace("Accounts selected")
        random.sleep(500, 1000)

        log.trace("Hide select accounts menu")
        accountSelectField.click()
        screenshot("alfa-26")

        log.trace("Click on download report button")
        wait.untilAppears("//*[@data-test-id='get-account-reports-button']").click()
        screenshot("alfa-27")
        Thread.sleep(1000) // simulate navigation
    }

    private fun getDownloadedFile(downloadPath: Path): File {
        log.trace("Getting downloaded file")
        log.trace("Timezone: {}", ZoneId.systemDefault())
        log.trace("Start: {}", periodAgo(lastTransactionDefaultPeriod))
        log.trace("End: {}", LocalDate.now())
        val start = periodAgo(lastTransactionDefaultPeriod).format(dateFormatter)
        val end = LocalDate.now().format(dateFormatter)
        val expectedName = "Statement $start - $end.xlsx"
        val reportPath = downloadPath.resolve(expectedName)
        log.trace("Looking for file '{}'", downloadPath.resolve(expectedName))

        fun getReport(): File {
            log.trace("File found!")
            val report = reportPath.toFile()
            report.setReadable(true)
            return report
        }

        return if (reportPath.exists()) {
            getReport()
        } else {
            var attempt = 0
            while (attempt < 3 && reportPath.notExists()) {
                log.trace("Wait {} for download", waitingDuration)
                Thread.sleep(waitingDuration.toMillis())
                if (reportPath.exists()) {
                    return getReport()
                } else {
                    attempt++
                }
            }
            throw IllegalStateException("Max attempts reached")
        }
    }
}