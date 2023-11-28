package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioSessionContext
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.time.Duration
import kotlin.random.Random

@Component
class TinkoffImportScenario(
    @Value("\${selenium-waiting-period:5s}") private val waitingDuration: Duration,
    private val tinkoffService: TinkoffService,
    private val sessionContext: ImportScenarioSessionContext
): ImportScenario {

    private val log = LoggerFactory.getLogger(TinkoffImportScenario::class.java)

    override fun startLogin(scenarioData: ImportScenarioData): Boolean {
        val random = Random(System.currentTimeMillis())

        Selenide.open("https://www.tinkoff.ru/mybank/")
        random.sleep(5000, 10000)

        Selenide.Wait().until { it.findElement(By.xpath("//button[@type='submit']")) }
        val phoneInput = `$`(By.xpath("//input[@automation-id='phone-input']"))
        random.sleep(4000, 6000)

        phoneInput.sendKeys(scenarioData.phone)
        phoneInput.pressEnter()
        random.sleep(4000, 6000)

        val otpInput = Selenide.Wait().until { `$`(By.xpath("//button[@automation-id='otp-input']")) }
        sessionContext.moveToState(TINKOFF_OTP_SENT)

        return otpInput != null
    }

    override fun confirmLoginAndImport(scenarioData: ImportScenarioData): Boolean {
        val random = Random(System.currentTimeMillis())
        val driver = sessionContext.getDriver()

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
        random.sleep(1500,3000)

        sessionContext.moveToState(TINKOFF_LOGIN_SUCCEED)
        try {
            val apiSession = driver.manage().getCookieNamed("api_session").value
            tinkoffService.importData(apiSession)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            return false
        } finally {
            driver.quit()
        }
        return true
    }

    private fun Random.sleep(from: Long, until: Long) {
        Thread.sleep(Duration.ofMillis(nextLong(from, until)))
    }
}