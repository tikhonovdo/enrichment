package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import ru.tikhonovdo.enrichment.domain.dto.transaction.BaseRecord
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class TinkoffRecord(
    override var draftTransactionId: Long? = null,
    override val operationDate: LocalDateTime,      // Дата операции (в UTC с 2024-05-05)
    val paymentDate: LocalDate?,                    // Дата платежа
    val accountNumber: String?,                     // Номер счета
    val cardNumber: String?,                        // Номер карты
    override val status: String,                    // Статус
    val operationSum: Double,                       // Сумма операции
    val operationCurrency: String,                  // Валюта операции
    override val paymentSum: Double,                // Сумма платежа
    val paymentCurrency: String,                    // Валюта платежа
    val cashback: Double?,                          // Кэшбэк
    override val category: String,                  // Категория
    override val mcc: String?,                      // MCC
    override val description: String,               // Описание
    val totalBonuses: Double,                       // Бонусы (включая кэшбэк)
    val roundingForInvestKopilka: Double,           // Округление на инвесткопилку
    val sumWithRoundingForInvestKopilka: Double,    // Сумма операции с округлением
    val message: String? = null,                    // Сообщение (актуально для переводов)
    val brandName: String? = null,                  // Название бренда
): BaseRecord(draftTransactionId, operationDate, status, paymentSum, category, mcc, description) {

    companion object {
        val operationDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss[.SSS]")
        fun parseOperationDate(value: String): LocalDateTime = operationDateTimeFormatter.parse(value, LocalDateTime::from)

        fun parseOperationDateToInstant(value: String, offset: ZoneOffset = ZoneOffset.ofHours(3)): Instant = parseOperationDate(value).toInstant(offset)
    }

    data class Raw(
        var operationDate: String? = null,
        var paymentDate: String? = null,
        var accountNumber: String? = null,
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

        var message: String? = null,               // Сообщение (актуально для переводов)
        var brandName: String? = null,             // Название бренда
    )
}