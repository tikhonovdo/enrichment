package ru.tikhonovdo.enrichment.service.importscenario.yandex

import com.codeborne.selenide.Selenide
import de.sstoehr.harreader.model.HarRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.config.ImportDataProperties
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaOperationRequest
import ru.tikhonovdo.enrichment.service.importscenario.AbstractImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

@Component
class YandexImportScenario(
    private val yandexProperties: ImportDataProperties,
    private val yandexService: YandexService,
    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.YANDEX) {

    private val targetRequestUrl = "${yandexProperties.apiUrl}/graphql"
    private val historyUrl = "${yandexProperties.apiUrl}/pay/history"
    private val otpInputXpath = "//input[@data-testid='DottedCodeInput_input']"

    init {
        stepActions = mapOf(
            START to Function { yaQrLogin() },
            OTP_SENT to Function { finishLoginAndSave(it) }
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun yaQrLogin(): ImportStepResult {
        val random = Random(System.currentTimeMillis())

        Selenide.open(yandexProperties.startUrl)
        random.sleep(2000, 5000)

        val driver = Selenide.webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("ya-01", driver)

        wait.untilAppears("//span[contains(text(),'QR')]/ancestor::button").click()
        random.sleep(2000, 5000)
        screenshot("ya-02", driver)

        val qr = wait.untilAppears("//div[@class='MagicField-qr']")

        GlobalScope.launch (Dispatchers.IO) {
            requestOtpCode(driver)
            resendOtpCodeRoutine(driver)
        }
        log.info("OTP sent")
        return ImportStepResult(OTP_SENT, qr.getCssValue("background"))
    }

    suspend fun requestOtpCode(driver: WebDriver) {
        log.info("Requesting OTP code in coroutine")

        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver, waitingDuration)
        random.sleep(2000, 5000)
        screenshot("ya-11", driver)

        // click on Login button
        wait.untilAppears("(//button[@class='UserID-Account']/ancestor::nav)//child::button").click()
        screenshot("ya-12", driver)
    }

    fun finishLoginAndSave(scenarioData: ImportScenarioData): ImportStepResult {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("ya-21")

        wait.untilAppears(otpInputXpath).sendKeys(scenarioData.otpCode)
        screenshot("ya-22")
        random.sleep(4000, 6000)

        screenshot("ya-23")
        return if (elementPresented("//a[contains(@href,'history')]")) {
            saveData(random, wait)
        } else {
            ImportStepResult(FAILURE)
        }
    }

    suspend fun resendOtpCodeRoutine(driver: WebDriver) {
        log.info("Resend OTP code routine started...")
        val timeout = Duration.ofSeconds(10)

        // fix broken auto sms sending
        var attempt = 1
        while (!elementPresented("//a[contains(@href,'history')]", driver, timeout)) {
            log.info("Trying to resend code: attempt #$attempt...")
            screenshot("ya-13-$attempt", driver)
            val wait = WebDriverWait(driver, waitingDuration)

            val resendButtonXpath = "$otpInputXpath/ancestor::div[5]//child::button[2]"
            val resendButton = wait.untilAppears(resendButtonXpath, suppressException = true)
            if (resendButton != null) {
                resendButton.click()
                log.info("Code resend success!")
                screenshot("ya-14", driver)
            }

            attempt++
        }
        log.info("Login succeed. Resend OTP code routine finished.")
    }

    fun saveData(random: Random, wait: WebDriverWait): ImportStepResult {
        driver().get(historyUrl)
        proxy().newHar()

        val payTransactions = wait.untilAppears("//button[contains(@title,'Карта Пэй')]")
        payTransactions.click()
        random.sleep(1000)
        screenshot("ya-24")

        val har = proxy().endHar()
        val transactionFeedRequestEntry = har.log.entries.last { isRequiredHar(it.request) }
        val operationRequest = JsonMapper.JSON_MAPPER.readValue(transactionFeedRequestEntry.request.postData.text, YaOperationRequest::class.java)

        val cookies = transactionFeedRequestEntry.request.headers
            .first { it.name.uppercase() == "COOKIE" }
            .value

        yandexService.importData(cookies, operationRequest)

        return ImportStepResult(DATA_SAVED)
    }

    private fun isRequiredHar(harRequest: HarRequest): Boolean {
        return try {
            val requestData = JsonMapper.JSON_MAPPER.readValue(harRequest.postData.text, YaOperationRequest::class.java)
            harRequest.url.startsWith(targetRequestUrl) && requestData.operationName == "GetTransactionFeedView" && requestData.variables?.filterType == "PAY_CARD"
        } catch (ignored: Throwable) {
            false
        }
    }

}