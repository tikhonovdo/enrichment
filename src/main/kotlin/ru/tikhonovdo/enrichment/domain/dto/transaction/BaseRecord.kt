package ru.tikhonovdo.enrichment.domain.dto.transaction

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class BaseRecord(
    open var draftTransactionId: Long? = null,
    open val operationDate: LocalDateTime, // Дата операции
    open val status: String?, // Статус
    open val paymentSum: Double, // Сумма платежа
    open val category: String, // Категория
    open val mcc: String?, // MCC код
    open val description: String, // Описание операции
) {

    companion object {
        internal val operationDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy[ HH:mm:ss]")
    }

}