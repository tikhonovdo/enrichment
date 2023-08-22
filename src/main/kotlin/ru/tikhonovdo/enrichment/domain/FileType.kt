package ru.tikhonovdo.enrichment.domain

enum class FileType(val bankId: Long?) {
    FINANCE_PM(null),
    TINKOFF(Bank.TINKOFF.id)
}