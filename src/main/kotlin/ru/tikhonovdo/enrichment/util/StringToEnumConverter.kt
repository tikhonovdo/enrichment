package ru.tikhonovdo.enrichment.util

import org.springframework.core.convert.converter.Converter
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState

class StringToBankEnumConverter: Converter<String, Bank> {
    override fun convert(source: String): Bank? {
        return Bank.entries.firstOrNull{ it.name == source.uppercase() }
    }
}

class StringToScenarioStateEnumConverter: Converter<String, ScenarioState> {
    override fun convert(source: String): ScenarioState? {
        return ScenarioState.entries.firstOrNull{ it.name == source.uppercase() }
    }
}