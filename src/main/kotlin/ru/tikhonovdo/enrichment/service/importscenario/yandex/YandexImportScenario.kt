package ru.tikhonovdo.enrichment.service.importscenario.yandex

import com.codeborne.selenide.Selenide
import de.sstoehr.harreader.model.HarRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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

    fun finishLoginAndSave(scenarioData: ImportScenarioData): ImportStepResult {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("ya-21")

        val otpInput = wait.untilAppears("//input[@data-testid='DottedCodeInput_input']")
        otpInput.sendKeys(scenarioData.otpCode)
        screenshot("ya-22")
        random.sleep(4000, 6000)

        screenshot("ya-23")
        return if (elementPresented("//a[contains(@href,'history')]")) {
            saveData(random)
        } else {
            ImportStepResult(FAILURE)
        }
    }

    fun saveData(random: Random): ImportStepResult {
        proxy().newHar()
        driver().get(historyUrl)
        random.sleep(1500)

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
            harRequest.url.startsWith(targetRequestUrl) && requestData.operationName == "GetTransactionFeedView"
        } catch (ignored: Throwable) {
            false
        }
    }

}