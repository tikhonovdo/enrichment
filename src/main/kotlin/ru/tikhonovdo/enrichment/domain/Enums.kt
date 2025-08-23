package ru.tikhonovdo.enrichment.domain

enum class Bank(val id: Long) {
    TINKOFF(1), ALFA(2), YANDEX(3)
}

enum class DataType(val bankId: Long?) {
    FINANCE_PM(null),
    TINKOFF(Bank.TINKOFF.id),
    ALFA(Bank.ALFA.id),
    YANDEX(Bank.YANDEX.id);

    companion object {
        fun fromBank(bank: Bank): DataType {
            return when (bank) {
                Bank.ALFA -> ALFA
                Bank.TINKOFF -> TINKOFF
                Bank.YANDEX -> YANDEX
            }
        }
    }
}

enum class Type(val id: Long) {
    INCOME(1L),
    OUTCOME(2L);

    companion object {
        fun swap(typeId: Long): Long {
            when (typeId) {
                INCOME.id -> return OUTCOME.id
                OUTCOME.id -> return INCOME.id
            }
            throw IllegalArgumentException("Unknown typeId passed: $typeId")
        }
    }
}

enum class Event(val id: Long) {
    TRANSFER(1L),
    DEBT_CREATION(2L),
    DEBT_REPAYMENT(3L),
    DEBT_INCREASING(4L),
    BALANCE_EDIT(5L)
}