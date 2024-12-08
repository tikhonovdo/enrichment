package ru.tikhonovdo.enrichment.service.importscenario

import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.using
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

interface ImportScenario {

    fun performImportStep(currentState: ScenarioState, scenarioData: ImportScenarioData): ImportStepResult

    fun getBank(): Bank

}

abstract class AbstractImportScenario(
    private val context: ImportScenarioContext,
    protected val waitingDuration: Duration,
    private val bank: Bank
): ImportScenario {

    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    protected lateinit var stepActions: Map<ScenarioState, Function<ImportScenarioData, ImportStepResult>>

    override fun getBank() = bank

    override fun performImportStep(currentState: ScenarioState, scenarioData: ImportScenarioData): ImportStepResult {
        return performImportStep(ImportStepResult(currentState), scenarioData)
    }

    fun performImportStep(previousStepResult: ImportStepResult, scenarioData: ImportScenarioData): ImportStepResult {
        checkState(previousStepResult.state)

        return try {
            val step = stepActions[previousStepResult.state] ?: throw IllegalStateException("Step ${previousStepResult.state} not found for $bank. Terminating...")
            val stepResult = step.apply(scenarioData)
            processStepResult(stepResult)
        } catch (e: Throwable) {
            log.warn("Error during import", e)
            screenshot("$bank-${previousStepResult.state}_failure")
            ImportStepResult(ScenarioState.FAILURE)
        }
    }

    protected fun driver() = context.getDriver()

    protected fun proxy() = context.getProxy()

    protected fun WebDriverWait.untilAppears(xPathExpression: String): WebElement {
        return until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathExpression)))
    }

    protected fun WebDriverWait.untilAppears(xPathExpression: String, fallbackOperation: Runnable): WebElement {
        var attempts = 0
        while (attempts < 3) {
            try {
                return until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPathExpression)))
            } catch (e: NoSuchElementException) {
                log.error(e.message, e)
                fallbackOperation.run()
                attempts++
            }
        }
        throw IllegalStateException("Max waiting attempts count reached")
    }

    protected fun elementPresented(xPathExpression: String, webDriver: WebDriver = driver()): Boolean {
        var presented = false
        try {
            WebDriverWait(webDriver, waitingDuration, Duration.ofSeconds(1)).untilAppears(xPathExpression)
            presented = true
        } catch (e: Exception) {
            log.error("Element not presented during operation with $bank", e)
        }
        return presented
    }

    protected fun Random.sleep(from: Long, until: Long) {
        Thread.sleep(nextLong(from, until))
    }

    protected fun screenshot(screenshotName: String, driver: WebDriver = driver()) {
        using(driver) { log.debug(Selenide.screenshot(screenshotName)) }
    }

    private fun checkState(state: ScenarioState) {
        context.checkState(state, bank)
    }

    fun processStepResult(stepResult: ImportStepResult): ImportStepResult {
        val nextState = context.switchState(stepResult.state, bank)
        return ImportStepResult(nextState, stepResult.payload)
    }
}