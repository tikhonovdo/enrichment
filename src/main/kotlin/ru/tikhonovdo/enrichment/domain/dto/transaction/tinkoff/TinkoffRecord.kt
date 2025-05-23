package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import ru.tikhonovdo.enrichment.domain.dto.transaction.BaseRecord
import java.time.LocalDate
import java.time.LocalDateTime

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
    val type: Type
): BaseRecord(draftTransactionId, operationDate, status, paymentSum, category, mcc, description) {

    enum class Type {
        DEBIT, // списание
        CREDIT; // пополнение

        fun toDomainType(): ru.tikhonovdo.enrichment.domain.Type {
            return when (this) {
                CREDIT -> ru.tikhonovdo.enrichment.domain.Type.INCOME
                DEBIT -> ru.tikhonovdo.enrichment.domain.Type.OUTCOME
            }
        }
    }
}

