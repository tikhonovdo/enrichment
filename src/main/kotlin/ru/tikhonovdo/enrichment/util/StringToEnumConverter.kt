package ru.tikhonovdo.enrichment.util

import org.springframework.core.convert.converter.Converter
import ru.tikhonovdo.enrichment.domain.Bank


class StringToBankEnumConverter: Converter<String, Bank> {
    override fun convert(source: String): Bank? {
        return Bank.values().firstOrNull{ it.name == source.uppercase() }
    }
}