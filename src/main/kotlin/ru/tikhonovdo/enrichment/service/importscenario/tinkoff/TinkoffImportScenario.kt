package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.Selenide.webdriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.AbstractImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

@Component
class TinkoffImportScenario(
    @Value("\${import.tinkoff.home-url}") private val homeUrl: String,
    private val tinkoffService: TinkoffService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.TINKOFF) {

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
        val random = Random(System.currentTimeMillis())

        open(homeUrl)
        random.sleep(2000, 5000)

        val driver = webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("tinkoff-01", driver)

        wait.untilAppears("//input[@automation-id='phone-input']").sendKeys(scenarioData.phone)
        screenshot("tinkoff-02", driver)

        wait.untilAppears("//button[@type='submit']").click()
        random.sleep(1500, 2000)

        screenshot("tinkoff-03", driver)
        return if (elementPresented("//input[@automation-id='otp-input']", driver)) {
            OTP_SENT
        } else {
            INITIAL
        }
    }

    fun finishLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val driver = driver()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("tinkoff-11")

        val otpInput = wait.untilAppears("//input[@automation-id='otp-input']")
        otpInput.sendKeys(scenarioData.otpCode)
        screenshot("tinkoff-12")
        random.sleep(4000, 6000)

        val passwordInput = wait.untilAppears("//input[@automation-id='password-input']")
        val buttonSubmit = wait.untilAppears("//button[@automation-id='button-submit']")
        screenshot("tinkoff-13")
        random.sleep(4000, 6000)

        passwordInput.sendKeys(scenarioData.password)
        buttonSubmit.click()
        screenshot("tinkoff-14")
        random.sleep(2500, 3500)

        wait.untilAppears("//button[@automation-id='cancel-button']").click()
        screenshot("tinkoff-15")
        Thread.sleep(5000) // simulate navigation

        val titlePresented = elementPresented("//*[@data-qa-type='desktop-ib-title']")
        screenshot("tinkoff-16")
        return if (titlePresented) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    fun saveData(): ScenarioState {
        try {
            val apiSession = driver().manage().getCookieNamed("api_session").value
            tinkoffService.importData(apiSession)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            return FAILURE
        }
        return DATA_SAVED
    }

}