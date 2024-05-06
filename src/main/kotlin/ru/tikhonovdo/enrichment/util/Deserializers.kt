package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsAdditionalDataRecord
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ZeroAsNullDeserializer: JsonDeserializer<Long?>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Long? {
        val value = parser.valueAsLong
        return if (value == 0L) {
            null
        } else {
            value
        }
    }
}

class MillisToLocalDateTimeDeserializer: JsonDeserializer<LocalDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDateTime {
        val value = parser.valueAsLong
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
    }
}

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