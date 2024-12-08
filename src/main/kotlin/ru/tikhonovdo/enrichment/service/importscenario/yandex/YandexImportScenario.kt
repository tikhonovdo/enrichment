package ru.tikhonovdo.enrichment.service.importscenario.yandex

import com.codeborne.selenide.Selenide
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import ru.tikhonovdo.enrichment.service.importscenario.AbstractImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

@Component
class YandexImportScenario(
    @Value("\${import.yandex.login-url}") private val yandexLoginUrl: String,
    @Value("\${import.yandex.url}") private val apiUrl: String,

    private val yandexService: YandexService,
    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.YANDEX) {

    private val targetRequestUrl = apiUrl + "graphql"

    init {
        stepActions = mapOf(
            START to Function { yaQrLogin() },
            OTP_SENT to Function { finishLogin(it) }
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun yaQrLogin(): ImportStepResult {
        val random = Random(System.currentTimeMillis())

        Selenide.open(yandexLoginUrl)
        random.sleep(2000, 5000)

        val driver = Selenide.webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("ya-01", driver)

        wait.untilAppears("//span[contains(text(),'QR')]/ancestor::button").click()
        random.sleep(2000, 5000)
        screenshot("ya-02", driver)

        val qr = wait.untilAppears("//div[@class='MagicField-qr']")

        GlobalScope.launch { async {
            requestOtpCode(driver)
        } }
        return ImportStepResult(OTP_SENT, qr.getCssValue("background"))
    }

    suspend fun requestOtpCode(driver: WebDriver): ImportStepResult {
        log.info("Requesting OTP code in coroutine")

        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver, waitingDuration)
        random.sleep(2000, 5000)
        screenshot("ya-11", driver)

        // click on Login button
        wait.untilAppears("(//button[@class='UserID-Account']/ancestor::nav)//child::button").click()
        screenshot("ya-12", driver)

        return ImportStepResult(OTP_SENT)
    }

    fun finishLogin(scenarioData: ImportScenarioData): ImportStepResult {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("ya-21")

        val otpInput = wait.untilAppears("//input[@data-testid='DottedCodeInput_input']")
        otpInput.sendKeys(scenarioData.otpCode)
        proxy()
        screenshot("ya-22")
        random.sleep(4000, 6000)

        screenshot("ya-23")
        return if (elementPresented("//a[contains(@href,'history')]")) {
            saveData()
        } else {
            ImportStepResult(FAILURE)
        }
    }

    fun saveData(): ImportStepResult {
        val operationsRequestEntry = proxy().endHar().log.entries.last { it.request.url == targetRequestUrl }

        val cookies = operationsRequestEntry.request.headers
            .first { it.name.uppercase() == "COOKIE" }
            .value

        yandexService.importData(cookies)

        return ImportStepResult(DATA_SAVED)
    }

}