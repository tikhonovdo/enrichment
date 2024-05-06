package ru.tikhonovdo.enrichment.service.importscenario

import com.codeborne.selenide.Selenide
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
    private var currentState: AtomicReference<ScenarioState> = AtomicReference(INITIAL)
    private var currentBank: AtomicReference<Bank?> = AtomicReference<Bank?>(null)

    @PreDestroy
    fun preDestroy() {
        log.info("Destroying scenario context...")
        resetContextWithState(DESTROYED)
    }

    fun checkState(expected: ScenarioState, bank: Bank?) {
        if (currentState.get() != expected) {
            throw IllegalStateException("Expected state '$expected', but current '$currentState'")
        }
        if (currentBank.get().let { it != null && it != bank }) {
            throw IllegalStateException("You trying process $bank, but current is $currentBank")
        }
    }

    fun switchState(nextState: ScenarioState, bank: Bank?): Boolean {
        if (!isAvailableMove(nextState, bank)) {
            return false
        }

        log.info("Changing state $currentState to $nextState, bank = $bank")
        currentState.set(nextState)
        currentBank.set(bank)
        when (nextState) {
            OTP_SENT -> {
                driver.set(Selenide.webdriver().`object`())
            }
            DATA_SAVED,
            FAILURE -> {
                log.info("Terminal state reached. Commit reset to initial state")
                resetContextWithState(INITIAL)
            }
            else -> { }
        }
        return true
    }

    fun getDriver(): WebDriver {
        return driver.get() ?: throw IllegalStateException("Oops! It seems that method called in wrong state. Current state: $currentState")
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
        driver.set(null)
        log.info("Session Chrome Driver destroyed")
    }

}