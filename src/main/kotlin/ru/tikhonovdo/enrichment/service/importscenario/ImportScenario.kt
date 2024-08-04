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
import java.time.Duration
import java.util.function.Function
import kotlin.random.Random

interface ImportScenario {

    fun performImportStep(stepName: String, scenarioData: ImportScenarioData): ScenarioState

    fun getBank(): Bank

}

abstract class AbstractImportScenario(
    private val context: ImportScenarioContext,
    protected val waitingDuration: Duration,
    private val bank: Bank
): ImportScenario {

    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    protected lateinit var stepActions: Map<String, Function<ImportScenarioData, ScenarioState>>

    override fun getBank() = bank

    override fun performImportStep(stepName: String, scenarioData: ImportScenarioData): ScenarioState {
        checkState(convertToState(stepName))

        val step = stepActions[stepName] ?: throw IllegalStateException("Step $stepName not found for $bank. Terminating...")

        val currentState = step.apply(scenarioData)
        return switchState(currentState)
    }

    private fun convertToState(stepName: String): ScenarioState {
        return ScenarioState.entries.first { it.stepName == stepName }
    }

    protected fun driver() = context.getDriver()

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
    fun checkState(state: ScenarioState) {
        context.checkState(state, bank)
    }

    fun switchState(state: ScenarioState): ScenarioState {
        return context.switchState(state, bank)
    }
}