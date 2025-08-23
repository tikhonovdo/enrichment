package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import lombok.NoArgsConstructor

@NoArgsConstructor
class TinkoffOperationsDataPayload(
    var resultCode: String,
    var payload: List<TinkoffOperationsRecord>
) {
    constructor() : this("", emptyList())
}

@JsonDeserialize(using = TinkoffOperationsRecordDeserializer::class)
class TinkoffOperationsRecord(
        var id: String,
        var authorizationId: String?,

        var operationTime: Long,           // Unix-time операции (millis)
        var debitingTime: Long?,           // Unix-time даты списания (millis)
        var type: String,                  // Дебит/кредит
        var cardNumber: String,            // Номер карты
        var status: String,                // Статус операции

        var operationSum: Double,
        var operationCurrency: String,
        var category: String,
        var categoryId: String,
        var mcc: Int?,
        var loyaltyBonusSummary: Double,

        var account: String,                // Номер счета
        var paymentSum: Double,             // Сумма операции в валюте счета
        var paymentCurrency: String,        // Код валюты счета
        var description: String,            // Описание
        var message: String?,               // Сообщение (актуально для переводов)
        var brandName: String?,             // Название бренда
        var senderDetails: String?,
        var hasReceipt: Boolean,
        var receipt: Any?
) {
    constructor() : this("", null, 0L, null, "","", "", Double.NaN,
            "", "","", null, Double.NaN, "", Double.NaN, "",
            "", null, null, null, false, null)
}
