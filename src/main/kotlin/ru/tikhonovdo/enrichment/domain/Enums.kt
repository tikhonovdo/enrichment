package ru.tikhonovdo.enrichment.domain

enum class Bank(val id: Long) {
    TINKOFF(1), ALFA(2)
}

enum class FileType(val bankId: Long?) {
    FINANCE_PM(null),
    TINKOFF(Bank.TINKOFF.id),
    ALFA(Bank.ALFA.id)
}

enum class Type(val id: Long) {
    INCOME(1L),
    OUTCOME(2L);
}

enum class Event(val id: Long) {
    TRANSFER(1L),
    DEBT_CREATION(2L),
    DEBT_REPAYMENT(3L),
    DEBT_INCREASING(4L),
    BALANCE_EDIT(5L)
}