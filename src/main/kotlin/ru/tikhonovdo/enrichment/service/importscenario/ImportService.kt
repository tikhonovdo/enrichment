package ru.tikhonovdo.enrichment.service.importscenario

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.tinkoff.TinkoffImportScenario

interface ImportService {

    fun login(bank: Bank, scenarioData: ImportScenarioData): Boolean

    fun confirmLoginAndImport(bank: Bank, scenarioData: ImportScenarioData): Boolean
}

@Component
class ImportServiceImpl(
    tinkoffImportScenario: TinkoffImportScenario,
): ImportService {

    private val bankToScenarioMap = mapOf(
        Bank.TINKOFF to tinkoffImportScenario
    )

    override fun login(bank: Bank, scenarioData: ImportScenarioData): Boolean {
        return bankToScenarioMap[bank]?.startLogin(scenarioData) ?: false
    }

    override fun confirmLoginAndImport(bank: Bank, scenarioData: ImportScenarioData): Boolean {
        return bankToScenarioMap[bank]?.confirmLoginAndImport(scenarioData) ?: false
    }
}