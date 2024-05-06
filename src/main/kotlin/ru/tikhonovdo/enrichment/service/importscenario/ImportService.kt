package ru.tikhonovdo.enrichment.service.importscenario

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.service.importscenario.alfabank.AlfabankImportScenario
import ru.tikhonovdo.enrichment.service.importscenario.tinkoff.TinkoffImportScenario
import java.time.LocalDateTime

interface ImportService {

    fun login(bank: Bank, scenarioData: ImportScenarioData): ScenarioState?

    fun confirmLoginAndImport(bank: Bank, scenarioData: ImportScenarioData): ScenarioState?

    fun getLastUpdateDate(bank: Bank): LocalDateTime?
}

@Component
class ImportServiceImpl(
    tinkoffImportScenario: TinkoffImportScenario,
    alfabankImportScenario: AlfabankImportScenario,
    private val draftTransactionRepository: DraftTransactionRepository
): ImportService {

    private val bankToScenarioMap = mapOf(
        Bank.TINKOFF to tinkoffImportScenario,
        Bank.ALFA to alfabankImportScenario
    )

    override fun login(bank: Bank, scenarioData: ImportScenarioData): ScenarioState? {
        return bankToScenarioMap[bank]?.startLogin(scenarioData)
    }

    override fun confirmLoginAndImport(bank: Bank, scenarioData: ImportScenarioData): ScenarioState? {
        return bankToScenarioMap[bank]?.confirmLoginAndImport(scenarioData)
    }

    override fun getLastUpdateDate(bank: Bank): LocalDateTime? {
        return draftTransactionRepository.getLastUpdateDate(bank)
    }
}