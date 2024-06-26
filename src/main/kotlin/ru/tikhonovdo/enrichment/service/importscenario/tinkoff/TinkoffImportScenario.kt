package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import com.codeborne.selenide.Selenide.open
import com.codeborne.selenide.Selenide.webdriver
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
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
import kotlin.random.Random

@Component
class TinkoffImportScenario(
    @Value("\${import.tinkoff.home-url}") private val homeUrl: String,
    private val tinkoffService: TinkoffService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.TINKOFF) {

    override fun requestOtpCode(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())

        open(homeUrl)
        random.sleep(2000, 5000)

        val driver = webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='phone-input']"))))
            .sendKeys(scenarioData.phone)
        wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@type='submit']"))))
            .click()
        random.sleep(3500, 5000)

        val otpInput = wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='otp-input']"))))
        return if (otpInput != null) {
            OTP_SENT
        } else {
            INITIAL
        }
    }

    override fun finishLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val driver = driver()
        val wait = WebDriverWait(driver, waitingDuration)

        val otpInput = wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='otp-input']"))))
        otpInput.sendKeys(scenarioData.otpCode)
        random.sleep(4000, 6000)

        val passwordInput = wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='password-input']"))))
        val buttonSubmit = wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@automation-id='button-submit']"))))
        random.sleep(6000, 8000)

        passwordInput.sendKeys(scenarioData.password)
        buttonSubmit.click()
        random.sleep(2500, 3500)

        val cancelButton = wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@automation-id='cancel-button']"))))
        cancelButton.click()
        Thread.sleep(1000) // simulate navigation

        return if (elementPresented("//*[@data-qa-type='navigation/username']")) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    override fun saveData(): ScenarioState {
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