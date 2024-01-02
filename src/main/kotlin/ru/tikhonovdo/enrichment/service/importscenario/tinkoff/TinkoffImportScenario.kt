package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import com.codeborne.selenide.Selenide.*
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.*
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
        WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='phone-input']"))))
            .sendKeys(scenarioData.phone)
        WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@type='submit']"))))
            .click()
        random.sleep(3500, 5000)

        val otpInput = WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='otp-input']"))))
        return if (otpInput != null) {
            OTP_SENT
        } else {
            INITIAL
        }
    }

    override fun finishLogin(scenarioData: ImportScenarioData): ScenarioState {
        val driver = driver()
        val random = Random(System.currentTimeMillis())

        val otpInput = WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='otp-input']"))))
        otpInput.sendKeys(scenarioData.otpCode)
        random.sleep(4000, 6000)

        val passwordInput = WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//input[@automation-id='password-input']"))))
        val buttonSubmit = WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@automation-id='button-submit']"))))
        random.sleep(6000,10000)

        passwordInput.sendKeys(scenarioData.password)
        buttonSubmit.click()
        random.sleep(2000,3000)

        val cancelButton = WebDriverWait(driver, waitingDuration)
            .until(ExpectedConditions.presenceOfElementLocated((By.xpath("//button[@automation-id='cancel-button']"))))
        cancelButton.click()
        Thread.sleep(1000) // simulate navigation

        return if (elementPresented("//*[@data-qa-type='desktop-homer-container' and @data-qa-state='success']")) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    override fun saveData(): Boolean {
        try {
            val apiSession = driver().manage().getCookieNamed("api_session").value
            tinkoffService.importData(apiSession)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            return false
        }
        return true
    }

}