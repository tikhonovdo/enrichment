package ru.tikhonovdo.enrichment.domain.dto.transaction

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AlfaRecord(
    override var draftTransactionId: Long? = null,
    override val operationDate: LocalDateTime, // Дата операции
    val paymentDate: LocalDate?, // Дата проводки
    val accountName: String, // Название счета
    val accountNumber: String, // Номер счета
    val cardName: String?, // Название карты
    val cardNumber: String?, // Номер карты
    override val description: String, // Описание операции
    override val paymentSum: Double, // Сумма платежа
    val paymentCurrency: String, // Валюта платежа
    override val status: String?, // Статус
    override val category: String, // Категория
    override val mcc: String?, // MCC код
    val type: String, // Тип (списание/начисление)
    val comment: String // Комментарий
): BaseRecord(draftTransactionId, operationDate, status, paymentSum, category, mcc, description) {

    companion object {
        private val operationDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss]")
        fun parseOperationDate(value: String): LocalDateTime = operationDateTimeFormatter.parse(value, LocalDateTime::from)
    }

    class Raw(
        var operationDate: String? = null,
        var paymentDate: String? = null,
        var accountName: String? = null,
        var accountNumber: String? = null,
        var cardName: String? = null,
        var cardNumber: String? = null,
        var description: String? = null,
        var paymentSum: Double? = null,
        var paymentCurrency: String? = null,
        var status: String? = null,
        var category: String? = null,
        var mcc: Int? = null,
        var type: String? = null,
        var comment: String? = null,
    )
}