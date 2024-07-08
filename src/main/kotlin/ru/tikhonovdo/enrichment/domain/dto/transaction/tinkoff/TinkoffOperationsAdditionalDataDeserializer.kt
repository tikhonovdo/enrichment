package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException

class TinkoffOperationsAdditionalDataDeserializer(vc: Class<*>?) :
    StdDeserializer<TinkoffOperationsAdditionalDataRecord>(vc) {

    constructor() : this(null)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): TinkoffOperationsAdditionalDataRecord {
        val recordNode: JsonNode = parser.codec.readTree(parser)
        val record = TinkoffOperationsAdditionalDataRecord()

        record.account = recordNode.get("account").textValue()
        record.accountAmount = recordNode.get("accountAmount").get("value").doubleValue()
        record.accountCurrencyCode = recordNode.get("accountAmount").get("currency").get("strCode").textValue()
        record.operationTime = recordNode.get("operationTime").get("milliseconds").longValue()
        record.description = recordNode.get("description").textValue()
        recordNode.get("cardNumber").let {
            if (it != null && !it.isNull) {
                record.cardNumber = it.textValue()
            }
        }
        recordNode.get("message").let {
            if (it != null && !it.isNull) {
                record.message = it.textValue()
            }
        }
        recordNode.get("brand").let {
            if (it != null && !it.isNull) {
                record.brandName = it.get("name").textValue()
            }
        }

        return record
    }

}