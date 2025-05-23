package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = TinkoffReceiptDataDeserializer::class)
class TinkoffReceiptData(
        var retailPlace: String?,
        var retailPlaceAddress: String?,
        var operator: String?,
        var totalSum: Double?,
        var items: Any?
) {
    constructor() : this(null, null, null, null, null)
}