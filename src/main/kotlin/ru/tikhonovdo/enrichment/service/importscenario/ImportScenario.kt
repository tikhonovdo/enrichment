package ru.tikhonovdo.enrichment.service.importscenario

interface ImportScenario {

    fun startLogin(scenarioData: ImportScenarioData): Boolean

    fun confirmLoginAndImport(scenarioData: ImportScenarioData): Boolean
}