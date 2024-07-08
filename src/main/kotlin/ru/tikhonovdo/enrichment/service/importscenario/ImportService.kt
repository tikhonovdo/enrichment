package ru.tikhonovdo.enrichment.service.importscenario

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.service.importscenario.alfabank.AlfabankImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.tinkoff.TinkoffImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.yandex.YandexImportScenario
import java.time.LocalDateTime

interface ImportService {
    fun performImportStep(bank: Bank, stepName: String, scenarioData: ImportScenarioData): ScenarioState?
    fun getLastUpdateDate(bank: Bank): LocalDateTime?
}

@Component
class ImportServiceImpl(
    tinkoffImportScenario: TinkoffImportScenario,
    alfabankImportScenario: AlfabankImportScenario,
    yandexImportScenario: YandexImportScenario,
    private val draftTransactionRepository: DraftTransactionRepository
): ImportService {

    private val bankToScenarioMap = listOf(
        tinkoffImportScenario,
        alfabankImportScenario,
        yandexImportScenario
    ).associateBy { it.bank }

    override fun performImportStep(bank: Bank, stepName: String, scenarioData: ImportScenarioData): ScenarioState? {
        return bankToScenarioMap[bank]?.performImportStep(stepName, scenarioData)
    }

    override fun getLastUpdateDate(bank: Bank): LocalDateTime? {
        return draftTransactionRepository.getLastUpdateDate(bank)
    }
}