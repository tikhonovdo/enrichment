package ru.tikhonovdo.enrichment.service.importscenario.yandex

import com.codeborne.selenide.Selenide.*
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

/**
 *
 * === Войти в аккаунт Яндекс ===
 *  1. Зайти на https://passport.yandex.ru/
 *  2. Нажать на "//button[@data-type='phone']" (войти по номеру)
 *  3. Задать в поле "//input[@id='passp-field-phone']" номер телефона
 *  4. Нажать на "//button[@type='submit']" (Войти)
 *  5. Отправить в поле "//input[@id='passp-field-phoneCode']" OTP код
 *  6. Нажать на "//<star>[@class='Accounts-list']/a"
 *  7. Подождать до 5 секунд загрузки главной страницы Пэя.
 *
 *  === Войти в аккаунт Пэй ===
 *  0. Зайти на https://bank.yandex.ru/pay/
 *  1. Нажать на "//button[@role='button']" (кнопка Войти)
 *  2. Дождаться СМС-кода и отправить его в сфокусированное поле "//input[@autocomplete='one-time-code']"
 *  3. Дождаться загрузки страницы - появления ссылки "//a[@href='/pay/debit']"
 * (4) Скопировать куки клиента и подставить их в запрос в Postman (Workspace Enrichment)
 * (5) Пример запроса лежит в scratch_4.json
 */
@Component
class YandexImportScenario(
    @Value("\${import.yandex.url}") private val homeUrl: String,
    @Value("\${import.yandex.passport}") private val passportUrl: String,
    private val yandexService: YandexService,

    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.YANDEX) {

    init {
        stepActions = mapOf(
            INITIAL.stepName to Function { saveData(it) },
            // анти-бот защита сделала все ниже бесполезным :(
//            INITIAL.stepName to Function { loginYaPassport(it) },
//            YA_PASSPORT_OTP_SENT.stepName to Function {
//                val newState = finishPassportLogin(it)
//                switchState(newState)
//                performImportStep(newState.stepName, it)
//            },
//            YA_PASSPORT_LOGIN_SUCCEED.stepName to Function { requestYaPayOtpCode(it) },
//            OTP_SENT.stepName to Function {
//                val newState = finishYaPayLogin(it)
//                switchState(newState)
//                performImportStep(newState.stepName, it)
//            },
//            LOGIN_SUCCEED.stepName to Function { saveData(null) }
        )
    }

    private fun loginYaPassport(scenarioData: ImportScenarioData): ScenarioState {
        /*
         *  1. Зайти на https://passport.yandex.ru/
         *  2. Нажать на "//button[@data-type='phone']" (войти по номеру)
         *  3. Задать в поле "//input[@id='passp-field-phone']" номер телефона
         *  4. Нажать на "//button[@type='submit']" (Войти)
         */
        val random = Random(System.currentTimeMillis())

        open(passportUrl)
        random.sleep(1000, 2000)

        val driver = webdriver().`object`()
        val wait = WebDriverWait(driver, waitingDuration)
        screenshot("ya-01", driver)

        wait.untilAppears("//button[@data-type='phone']") { driver.navigate().refresh() }.click()
        screenshot("ya-02", driver)
        random.sleep(1200, 1500)

        wait.untilAppears("//input[@id='passp-field-phone']").sendKeys(scenarioData.phone)
        screenshot("ya-03", driver)
        random.sleep(1500, 2500)

        wait.untilAppears("//button[@type='submit']").click()
        screenshot("ya-04", driver)
        random.sleep(1500, 2000)

        screenshot("ya-05", driver)
        return if (elementPresented("//input[@id='passp-field-phoneCode']", driver)) {
            YA_PASSPORT_OTP_SENT
        } else {
            INITIAL
        }
    }

    fun finishPassportLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("ya-11")

        wait.untilAppears("//input[@id='passp-field-phoneCode']").sendKeys(scenarioData.otpCode)
        screenshot("ya-12")
        random.sleep(700, 1500)

        wait.untilAppears("//*[@class='Accounts-list']/a").click()
        screenshot("ya-13")
        random.sleep(700, 1500)

        wait.untilAppears("//*[data-t='button:pseudo']").click()
        screenshot("ya-14")
        random.sleep(1200, 1500)

        val phoneElement = wait.untilAppears("//*[data-testid='phone']")
        val phone = phoneElement.text.replace(Regex("[- ]"), "")
        val loginSuccess = scenarioData.phone == phone

        screenshot("ya-15")
        return if (loginSuccess) {
            YA_PASSPORT_LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    fun requestYaPayOtpCode(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)

        using(driver()) { open("$homeUrl/pay/") }
        random.sleep(1000, 2000)
        screenshot("ya-21")

        wait.untilAppears("//div[@class='UserID-Wrapper']/../../div/button").click()
        random.sleep(500, 1000)
        screenshot("ya-22")

        val otpInputPresented = elementPresented("//input[@autocomplete='one-time-code']")
        screenshot("ya-23")
        return if (otpInputPresented) {
            OTP_SENT
        } else {
            INITIAL
        }
    }

    fun finishYaPayLogin(scenarioData: ImportScenarioData): ScenarioState {
        val random = Random(System.currentTimeMillis())
        val wait = WebDriverWait(driver(), waitingDuration)
        screenshot("ya-31")

        wait.untilAppears("//input[@autocomplete='one-time-code']").sendKeys(scenarioData.otpCode)
        random.sleep(800, 1200)
        screenshot("ya-32")
        return if (elementPresented("//a[@href='/pay/debit']")) {
            LOGIN_SUCCEED
        } else {
            INITIAL
        }
    }

    fun saveData(scenarioData: ImportScenarioData?): ScenarioState {
        try {
            val cookie: String = if (scenarioData != null) {
                scenarioData.cookie!!
            } else {
                StringBuilder().let { stringBuilder ->
                    driver().manage().cookies.forEach {
                        stringBuilder
                            .append(it.name)
                            .append("=")
                            .append(it.value)
                            .append("; ")
                    }
                    return@let stringBuilder.toString()
                }
            }
            yandexService.importData(cookie)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            return INITIAL
        }
        return DATA_SAVED
    }

}