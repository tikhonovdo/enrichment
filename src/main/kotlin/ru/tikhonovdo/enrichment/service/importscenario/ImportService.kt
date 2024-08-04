package ru.tikhonovdo.enrichment.service.importscenario

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import java.time.LocalDateTime

interface ImportService {
    fun performImportStep(bank: Bank, stepName: String, scenarioData: ImportScenarioData): ScenarioState?
    fun getLastUpdateDate(bank: Bank): LocalDateTime?
}

@Component
class ImportServiceImpl(
    importScenarios: Collection<ImportScenario>,
    private val draftTransactionRepository: DraftTransactionRepository
): ImportService {

    private val bankToScenarioMap = importScenarios.associateBy { it.getBank() }

    override fun performImportStep(bank: Bank, stepName: String, scenarioData: ImportScenarioData): ScenarioState? {
        return bankToScenarioMap[bank]?.performImportStep(stepName, scenarioData)
    }

    override fun getLastUpdateDate(bank: Bank): LocalDateTime? {
        return draftTransactionRepository.getLastUpdateDate(bank)
    }
}