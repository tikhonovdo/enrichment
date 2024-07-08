package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import lombok.NoArgsConstructor

@NoArgsConstructor
class TinkoffOperationsAdditionalDataPayload(
    var resultCode: String,
    var payload: List<TinkoffOperationsAdditionalDataRecord>
)

@JsonDeserialize(using = TinkoffOperationsAdditionalDataDeserializer::class)
class TinkoffOperationsAdditionalDataRecord(
    var operationTime: Long?,           // Unix-time операции (millis)
    var cardNumber: String?,            // Номер карты
    var account: String,                // Номер счета
    var accountAmount: Double,          // Сумма операции в валюте счета
    var accountCurrencyCode: String,    // Код валюты счета
    var description: String,            // Описание
    var message: String?,               // Сообщение (актуально для переводов)
    var brandName: String?,             // Название бренда
) {
    constructor() : this(null, null, "", Double.NaN, "", "", null, null)
}