package ru.tikhonovdo.enrichment.service.importscenario

import com.codeborne.selenide.Selenide
import jakarta.annotation.PreDestroy
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.*
import java.util.concurrent.atomic.AtomicReference

@SessionScope
@Component
class ImportScenarioSessionContext {

    private val log = LoggerFactory.getLogger(ImportScenarioSessionContext::class.java)

    private val driver: AtomicReference<WebDriver?> = AtomicReference<WebDriver?>()
    private var currentState: ScenarioState = INITIAL

    fun moveToState(nextState: ScenarioState): Boolean {
        if (!isAvailableMove(nextState)) {
            return false
        }

        when (nextState) {
            TINKOFF_OTP_SENT -> {
                driver.set(Selenide.webdriver().`object`())
            }
            TINKOFF_LOGIN_SUCCEED -> {
                driver.set(null)
                currentState = INITIAL
            }
            else -> throw IllegalStateException("Cannot move to state $nextState from $currentState")
        }
        return true
    }

    fun getDriver(): WebDriver {
        return driver.get() ?: throw IllegalStateException("Oops! It seems that method called in wrong state. Current state: $currentState")
    }

    private fun isAvailableMove(nextState: ScenarioState) =
        currentState.weight < nextState.weight && (currentState.bank == null || currentState.bank == nextState.bank)

    @PreDestroy
    fun preDestroy() {
        currentState = DESTROYED
        driver.get()?.quit()
        driver.set(null)
        log.info("Session Chrome Driver destroyed")
    }

}