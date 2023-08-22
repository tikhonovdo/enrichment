package ru.tikhonovdo.enrichment.domain.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class TinkoffRecord(
    val operationDate: LocalDateTime,            // Дата операции
    val paymentDate: LocalDate,                  // Дата платежа
    val cardNumber: String,                      // Номер карты
    val status: String,                          // Статус
    val operationSum: Double,                    // Сумма операции
    val operationCurrency: String,               // Валюта операции
    val paymentSum: Double,                      // Сумма платежа
    val paymentCurrency: String,                 // Валюта платежа
    val cashback: Int,                           // Кэшбэк
    val category: String,                        // Категория
    val mcc: String,                             // MCC
    val description: String,                     // Описание
    val totalBonuses: Double,                    // Бонусы (включая кэшбэк)
    val roundingForInvestKopilka: Double,        // Округление на инвесткопилку
    val sumWithRoundingForInvestKopilka: Double  // Сумма операции с округлением
) {

    class Raw(
        var operationDate: String? = null,
        var paymentDate: String? = null,
        var cardNumber: String? = null,
        var status: String? = null,
        var operationSum: Double? = null,
        var operationCurrency: String? = null,
        var paymentSum: Double? = null,
        var paymentCurrency: String? = null,
        var cashback: Double? = null,
        var category: String? = null,
        var mcc: Int? = null,
        var description: String? = null,
        var totalBonuses: Double? = null,
        var roundingForInvestKopilka: Double? = null,
        var sumWithRoundingForInvestKopilka: Double? = null,
    )

    fun toCashTransfer(): TinkoffRecord {
        return TinkoffRecord(
            operationDate,
            paymentDate,
            "rub_cash",
            status,
            operationSum * -1,
            operationCurrency,
            paymentSum * -1,
            paymentCurrency,
            cashback,
            category,
            mcc,
            description,
            totalBonuses,
            roundingForInvestKopilka,
            sumWithRoundingForInvestKopilka * -1
        )
    }
}