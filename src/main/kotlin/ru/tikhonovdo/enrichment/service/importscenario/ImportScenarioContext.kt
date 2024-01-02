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
    private var currentState: ScenarioState = INITIAL
    private var currentBank: Bank? = null

    @PreDestroy
    fun preDestroy() {
        switchState(DESTROYED, null)
    }

    fun checkState(expected: ScenarioState, bank: Bank?) {
        if (currentState != expected) {
            throw IllegalStateException("Expected state '$expected', but current '$currentState'")
        }
        if (currentBank != null && currentBank != bank) {
            throw IllegalStateException("You trying process $bank, but current is $currentBank")
        }
    }

    fun switchState(nextState: ScenarioState, bank: Bank?): Boolean {
        if (!isAvailableMove(nextState, bank)) {
            return false
        }

        currentState = nextState
        currentBank = bank
        when (currentState) {
            OTP_SENT -> {
                driver.set(Selenide.webdriver().`object`())
            }
            DESTROYED -> {
                currentBank = null
                destroyDriver()
            }
            else -> { }
        }
        return true
    }

    fun getDriver(): WebDriver {
        return driver.get() ?: throw IllegalStateException("Oops! It seems that method called in wrong state. Current state: $currentState")
    }

    private fun isAvailableMove(nextState: ScenarioState, bank: Bank?) =
        nextState == DESTROYED ||
                currentState.weight < nextState.weight && (currentBank == null || currentBank == bank)

    private fun destroyDriver() {
        driver.get()?.quit()
        driver.set(null)
        log.info("Session Chrome Driver destroyed")
    }

}