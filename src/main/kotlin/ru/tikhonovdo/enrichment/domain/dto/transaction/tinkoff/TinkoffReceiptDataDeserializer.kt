package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.tikhonovdo.enrichment.util.getDoubleValue
import ru.tikhonovdo.enrichment.util.getTextValue
import java.io.IOException

class TinkoffReceiptDataDeserializer(vc: Class<*>?) :
    StdDeserializer<TinkoffReceiptData>(vc) {

    constructor() : this(null)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): TinkoffReceiptData {
        val recordNode: JsonNode = parser.codec.readTree(parser)
        val data = TinkoffReceiptData()

        val receiptNode = recordNode.get("payload").get("receipt")

        data.retailPlace = receiptNode.getTextValue("retailPlace")
        data.retailPlaceAddress = receiptNode.getTextValue("retailPlaceAddress")
        data.operator = receiptNode.getTextValue("operator")
        data.totalSum = receiptNode.getDoubleValue("totalSum")
        data.items = receiptNode.get("items")

        return data
    }

}