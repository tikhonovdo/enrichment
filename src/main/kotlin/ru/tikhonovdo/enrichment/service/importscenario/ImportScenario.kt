package ru.tikhonovdo.enrichment.service.importscenario

import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.domain.Bank
import java.time.Duration
import kotlin.random.Random

interface ImportScenario {

    fun startLogin(scenarioData: ImportScenarioData): ScenarioState

    fun confirmLoginAndImport(scenarioData: ImportScenarioData): ScenarioState
}

abstract class AbstractImportScenario(
    private val context: ImportScenarioContext,
    protected val waitingDuration: Duration,
    val bank: Bank
): ImportScenario {

    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun startLogin(scenarioData: ImportScenarioData): ScenarioState {
        checkState(ScenarioState.INITIAL)

        val newState = requestOtpCode(scenarioData)
        switchState(newState)

        return newState
    }

    override fun confirmLoginAndImport(scenarioData: ImportScenarioData): ScenarioState {
        checkState(ScenarioState.OTP_SENT)

        var newState = finishLogin(scenarioData)
        switchState(newState)

        checkState(ScenarioState.LOGIN_SUCCEED)
        newState = saveData()
        switchState(newState)

        return newState
    }

    abstract fun saveData(): ScenarioState

    abstract fun requestOtpCode(scenarioData: ImportScenarioData): ScenarioState

    abstract fun finishLogin(scenarioData: ImportScenarioData): ScenarioState

    protected fun driver() = context.getDriver()

    protected fun elementPresented(xPathExpression: String): Boolean {
        var presented = false
        try {
            WebDriverWait(driver(), waitingDuration)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathExpression)))
            presented = true
        } catch (e: Exception) {
            log.error("Element not presented during operation with $bank", e)
        }
        return presented

    }

    protected fun Random.sleep(from: Long, until: Long) {
        Thread.sleep(Duration.ofMillis(nextLong(from, until)))
    }

    private fun checkState(state: ScenarioState) {
        context.checkState(state, bank)
    }

    private fun switchState(state: ScenarioState) {
        context.switchState(state, bank)
    }
}