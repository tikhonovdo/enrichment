package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import ru.tikhonovdo.enrichment.domain.dto.transaction.BaseRecord
import java.time.LocalDate
import java.time.LocalDateTime

data class YandexRecord(
    override var draftTransactionId: Long? = null,
    override val operationDate: LocalDateTime, // Дата операции в UTC
    val paymentDate: LocalDate?, // Дата проводки
    val accountName: String, // Название счета
    override val description: String, // Описание операции
    override val paymentSum: Double, // Сумма платежа
    val paymentCurrency: String, // Валюта платежа
    override val status: String?, // Статус
    override val category: String, // Категория
    override val mcc: String?, // MCC код
    val direction: Direction, // Тип (списание/начисление)
    val comment: String? // Комментарий
): BaseRecord(draftTransactionId, operationDate, status, paymentSum, category, mcc, description) {

    companion object {
        @JvmStatic
        val ACCOUNT_NAME = "yandex_debit_account";
    }

}