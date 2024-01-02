package ru.tikhonovdo.enrichment.domain.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AlfaRecord(
    var draftTransactionId: Long? = null,
    val operationDate: LocalDate, // Дата операции
    val paymentDate: LocalDate?, // Дата проводки
    val accountName: String, // Название счета
    val accountNumber: String, // Номер счета
    val cardName: String?, // Название карты
    val cardNumber: String?, // Номер карты
    val description: String, // Описание операции
    val operationSum: Double, // Сумма
    val operationCurrency: String, // Валюта
    val status: String?, // Статус
    val category: String, // Категория
    val mcc: Int?, // MCC код
    val type: String, // Тип (списание/начисление)
    val comment: String // Комментарий
) {

    companion object {
        private val operationDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        fun parseOperationDate(value: String): LocalDateTime = operationDateTimeFormatter.parse(value, LocalDate::from).atStartOfDay()
    }

    class Raw(
        var operationDate: String? = null,
        var paymentDate: String? = null,
        var accountName: String? = null,
        var accountNumber: String? = null,
        var cardName: String? = null,
        var cardNumber: String? = null,
        var description: String? = null,
        var operationSum: Double? = null,
        var operationCurrency: String? = null,
        var status: String? = null,
        var category: String? = null,
        var mcc: Int? = null,
        var type: String? = null,
        var comment: String? = null,
    )
}