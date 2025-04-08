package ru.tikhonovdo.enrichment.service.importscenario

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.ImportStepResult
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.time.LocalDateTime

interface ImportService {
    fun performImportStep(bank: Bank, currentState: ScenarioState, scenarioData: ImportScenarioData): ImportStepResult?
    fun getDraftTransactionLastUploadDate(bank: Bank): LocalDateTime?
    fun getTransactionLastMatchedDate(bank: Bank): LocalDateTime?
}

@Component
class ImportServiceImpl(
    importScenarios: Collection<ImportScenario>,
    private val draftTransactionRepository: DraftTransactionRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository
): ImportService {

    private val bankToScenarioMap = importScenarios.associateBy { it.getBank() }

    override fun performImportStep(bank: Bank, currentState: ScenarioState, scenarioData: ImportScenarioData): ImportStepResult? {
        return bankToScenarioMap[bank]?.performImportStep(currentState, scenarioData)
    }

    override fun getDraftTransactionLastUploadDate(bank: Bank): LocalDateTime? {
        return draftTransactionRepository.getLastUploadDate(bank)
    }

    override fun getTransactionLastMatchedDate(bank: Bank): LocalDateTime? {
        return transactionMatchingRepository.getLastMatchedDate(bank.id)
    }

}