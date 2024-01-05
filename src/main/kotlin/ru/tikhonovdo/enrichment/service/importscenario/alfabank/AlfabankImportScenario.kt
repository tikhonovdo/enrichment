package ru.tikhonovdo.enrichment.service.importscenario.alfabank

import com.codeborne.selenide.Selenide.*
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.FileSystemUtils
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.service.file.FileService
import ru.tikhonovdo.enrichment.service.importscenario.*
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.io.File
import java.net.URI
import java.nio.file.*
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.random.Random

@Component
class AlfabankImportScenario(
    @Value("\${selenoid-host-download-path}") private val hostDownloadPath: String,
    @Value("\${import.alfa.home-url}") private val homeUrl: String,
    private val fileService: FileService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
) : AbstractImportScenario(context, waitingDuration, Bank.ALFA) {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun requestOtpCode(scenarioData: ImportScenarioData): ScenarioState {
        resetDownloadPath()
        runRequestOtpCodeScenario(scenarioData)

        val otpInput = WebDriverWait(webdriver().`object`(), waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@autocomplete='one-time-code']")))
        return if (otpInput != null) {
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
        random.sleep(2000, 5000)

        val driver = webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@type='text']"))))
            .sendKeys(scenarioData.login)
        wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@type='password']"))))
            .sendKeys(scenarioData.password)
        random.sleep(2500, 4000)

        wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@type='submit']"))))
            .click()
        random.sleep(1000, 1500) // wait for request complete
    }

    override fun finishLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())

        scenarioData.otpCode!!
            .map { it.toString() }
            .forEachIndexed { index: Int, char: String ->
                WebDriverWait(driver(), waitingDuration)
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//input)[${index + 1}]")))
                    .sendKeys(char)
                random.sleep(80, 150)
            }
        Thread.sleep(1000) // simulate navigation

        return if (elementPresented("//*[@data-test-id='main-menu-layout-header-root']")) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    override fun saveData(): ScenarioState {
        val watchService = FileSystems.getDefault().newWatchService()
        val downloadPath = Path(hostDownloadPath)
        downloadPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE)

        saveDataScenario()

        val report = getDownloadedFile(watchService, downloadPath)
        try {
            fileService.saveData(report.readBytes(), FileType.ALFA)
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

        driver.get(URI.create(driver.currentUrl).resolve("history").toString())
        random.sleep(1000, 1500) // wait for the next form

        val operationsReportLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test-id='categories-group']/../button")))
        random.sleep(1000, 2000) // simulate pointing on link

        operationsReportLink.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//*[@data-test-id='quick-period-tags']//following-sibling::button)[last()]")))
            .click()
        random.sleep(500, 1000)

        val accountSelectField = wait
            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test-id='account-select-field']")))
        random.sleep(1000, 2000) // simulate pointing on link

        accountSelectField.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test-id='account-select-option']")))
            .click()
        random.sleep(500, 1000)

        accountSelectField.click()
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-test-id='get-account-reports-button']")))
            .click()

        // just prolongate webdriver life
        Thread.sleep(waitingDuration)
        accountSelectField.isDisplayed
    }

    private fun getDownloadedFile(watchService: WatchService, downloadPath: Path): File {
        val start = sixMonthsAgo().format(dateFormatter)
        val end = LocalDate.now().format(dateFormatter)
        val expectedName = "Statement $start - $end.xlsx"

        var key: WatchKey?
        var reportPath: Path? = null
        while (watchService.take().also { key = it } != null && reportPath != null) {
            for (event in key!!.pollEvents()) {
                if (event.context().toString() == expectedName) {
                    reportPath = downloadPath.resolve(expectedName)
                }
            }
            key!!.reset()
        }
        watchService.close()

        val report = downloadPath.resolve(expectedName).toFile()
        report.setReadable(true)

        return report
    }
}