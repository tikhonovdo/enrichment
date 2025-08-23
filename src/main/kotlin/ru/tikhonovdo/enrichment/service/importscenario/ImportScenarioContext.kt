package ru.tikhonovdo.enrichment.service.importscenario

import com.browserup.bup.BrowserUpProxy
import com.browserup.bup.proxy.CaptureType
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import jakarta.annotation.PreDestroy
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.util.concurrent.atomic.AtomicReference

@SessionScope
@Component
class ImportScenarioContext {

    private val log = LoggerFactory.getLogger(ImportScenarioContext::class.java)

    private val driver: AtomicReference<WebDriver?> = AtomicReference<WebDriver?>()
    private val proxy: AtomicReference<BrowserUpProxy?> = AtomicReference<BrowserUpProxy?>()
    private var currentState: AtomicReference<ScenarioState> = AtomicReference(START)
    private var currentBank: AtomicReference<Bank?> = AtomicReference<Bank?>(null)

    @PreDestroy
    fun preDestroy() {
        log.info("Destroying scenario context...")
        resetContextWithState(DESTROYED)
    }

    fun checkState(expected: ScenarioState, bank: Bank?) {
        if (currentBank.get().let { it != null && it != bank }) {
            throw IllegalStateException("You trying process $bank, but current is $currentBank")
        }
    }

    fun switchState(nextState: ScenarioState, bank: Bank?): ScenarioState {
        if (!isAvailableMove(nextState, bank)) {
            log.warn("Cannot switch to state $nextState for bank $bank: current state is $currentState and current bank is $currentBank")
            return currentState.get()
        }

        log.info("Changing state $currentState to $nextState, bank = $bank")
        currentState.set(nextState)
        currentBank.set(bank)
        when (nextState) {
            OTP_SENT -> {
                driver.set(Selenide.webdriver().`object`())
                if (setOf(Bank.ALFA, Bank.YANDEX).contains(bank)) {
                    proxy.set(initProxy())
                }
            }
            DATA_SAVED,
            FAILURE -> {
                log.info("Terminal state reached. Commit reset to initial state")
                resetContextWithState(START)
            }
            else -> { }
        }
        return nextState
    }

    private fun initProxy(): BrowserUpProxy {
        val proxy = WebDriverRunner.getSelenideProxy().proxy
        proxy.setHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT)
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT)
        return proxy
    }

    fun getDriver(): WebDriver {
        return driver.get() ?: throw IllegalStateException("Oops! It seems that method called in wrong state. Current state: $currentState")
    }

    fun getProxy(): BrowserUpProxy {
        return proxy.get() ?: throw IllegalStateException("Oops! It seems that method called in wrong state. Current state: $currentState")
    }

    private fun isAvailableMove(nextState: ScenarioState, bank: Bank?) =
        currentState.get().weight < nextState.weight && (currentBank.get().let { it == null || it == bank })

    fun resetContextWithState(state: ScenarioState) {
        currentState.set(state)
        currentBank.set(null)
        destroyDriver()
    }

    private fun destroyDriver() {
        driver.get()?.quit()
        proxy.get()?.stop()
        driver.set(null)
        proxy.set(null)
        log.info("Session Chrome Driver destroyed")
    }

}