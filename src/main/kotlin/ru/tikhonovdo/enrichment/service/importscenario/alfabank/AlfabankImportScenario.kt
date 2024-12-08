package ru.tikhonovdo.enrichment.service.importscenario.alfabank

import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.Selenide.webdriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import ru.tikhonovdo.enrichment.service.importscenario.AbstractImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.net.URI
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

@Component
class AlfabankImportScenario(
    @Value("\${import.alfa.home-url}") private val homeUrl: String,
    @Value("\${import.alfa.api-url}") private val apiUrl: String,
    private val alfaService: AlfaService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.ALFA) {

    private val targetRequestUrl = apiUrl + "operations-history/operations"

    init {
        stepActions = mapOf(
            START to Function { requestOtpCode(it) },
            OTP_SENT to Function {
                val stepResult = finishLogin(it)
                performImportStep(processStepResult(stepResult), it)
            },
            LOGIN_SUCCEED to Function { saveData() }
        )
    }

    fun requestOtpCode(scenarioData: ImportScenarioData): ImportStepResult {
        val random = Random(System.currentTimeMillis())

        open(homeUrl)
        random.sleep(2000, 5000)

        val driver = webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("alfa-01", driver)

        wait.untilAppears("//input[@type='text']").sendKeys(scenarioData.login)
        wait.untilAppears("//input[@type='password']").sendKeys(scenarioData.password)
        screenshot("alfa-02", driver)
        random.sleep(2500, 4000)

        wait.untilAppears("//button[@type='submit']").click()
        random.sleep(1000, 1500) // wait for request complete

        screenshot("alfa-03", driver)
        return ImportStepResult(
            if (elementPresented("//input[@autocomplete='one-time-code']", driver)) {
                OTP_SENT
            } else {
                START
            }
        )
    }

    fun finishLogin(scenarioData: ImportScenarioData): ImportStepResult {
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

        return ImportStepResult(
            if (elementPresented("//*[@data-test-id='main-menu-layout-header-root']")) {
                LOGIN_SUCCEED
            } else {
                START
            }
        )
    }

    fun saveData(): ImportStepResult {
        log.trace("Open history page")
        driver().get(URI.create(driver().currentUrl).resolve("history").toString())
        screenshot("alfa-21")

        val operationsRequestEntry = proxy().endHar().log.entries.first { it.request.url == targetRequestUrl }

        val xsrf = operationsRequestEntry.request.headers
            .first { it.name.uppercase() == "X-XSRF-TOKEN" }
            .value

        val cookies = operationsRequestEntry.request.headers
            .first { it.name.uppercase() == "COOKIE" }
            .value

        alfaService.importData(cookies, xsrf)

        screenshot("alfa-30-success")
        return ImportStepResult(DATA_SAVED)
    }

}