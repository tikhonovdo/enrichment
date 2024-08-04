package ru.tikhonovdo.enrichment.service.importscenario.yandex

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.AbstractImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioContext
import ru.tikhonovdo.enrichment.service.importscenario.ImportScenarioData
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.DATA_SAVED
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState.INITIAL
import java.time.Duration
import java.util.function.Function

@Component
class YandexImportScenario(
    private val yandexService: YandexService,
    @Value("\${selenium-waiting-period:5s}") waitingDuration: Duration,
    context: ImportScenarioContext
): AbstractImportScenario(context, waitingDuration, Bank.YANDEX) {

    init {
        stepActions = mapOf(
            INITIAL.stepName to Function { saveData(it) }
        )
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