package ru.tikhonovdo.enrichment.domain.dto.transaction

import java.time.LocalDate
import java.time.LocalDateTime

data class TinkoffRecord(
    override var draftTransactionId: Long? = null,
    override val operationDate: LocalDateTime,            // Дата операции
    val paymentDate: LocalDate?,                 // Дата платежа
    val cardNumber: String?,                     // Номер карты
    override val status: String,                          // Статус
    val operationSum: Double,                    // Сумма операции
    val operationCurrency: String,               // Валюта операции
    override val paymentSum: Double,                      // Сумма платежа
    val paymentCurrency: String,                 // Валюта платежа
    val cashback: Double?,                       // Кэшбэк
    override val category: String,                        // Категория
    override val mcc: String?,                            // MCC
    override val description: String,                     // Описание
    val totalBonuses: Double,                    // Бонусы (включая кэшбэк)
    val roundingForInvestKopilka: Double,        // Округление на инвесткопилку
    val sumWithRoundingForInvestKopilka: Double  // Сумма операции с округлением
): BaseRecord(draftTransactionId, operationDate, status, paymentSum, category, mcc, description) {

    companion object {
        fun parseOperationDate(value: String): LocalDateTime = operationDateTimeFormatter.parse(value, LocalDateTime::from)
    }

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
}