package ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.tikhonovdo.enrichment.util.*
import java.io.IOException

class TinkoffOperationsRecordDeserializer(vc: Class<*>?) :
    StdDeserializer<TinkoffOperationsRecord>(vc) {

    constructor() : this(null)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): TinkoffOperationsRecord {
        val recordNode: JsonNode = parser.codec.readTree(parser)
        val record = TinkoffOperationsRecord()

        record.id = recordNode.getTextValue("id")
        record.type = recordNode.getTextValue("type")
        record.account = recordNode.getTextValue("account")
        record.paymentSum = recordNode.getDoubleValue("accountAmount.value")
        record.paymentCurrency = recordNode.getTextValue("accountAmount.currency.name")
        record.operationTime = recordNode.getLongValue("operationTime.milliseconds")
        record.debitingTime = recordNode.getLongValue("debitingTime.milliseconds")
        record.status = recordNode.getTextValue("status")
        record.operationSum = recordNode.getDoubleValue("amount.value")
        record.operationCurrency = recordNode.getTextValue("amount.currency.name")
        record.category = recordNode.getTextValue("spendingCategory.name")
        record.categoryId = recordNode.getTextValue("spendingCategory.id")
        record.mcc = recordNode.getIntValue("mcc")
        record.loyaltyBonusSummary = recordNode.getDoubleValue("loyaltyBonusSummary.amount")
        record.description = recordNode.getTextValue("description")
        record.cardNumber = recordNode.getTextValue("cardNumber")
        record.message = recordNode.getField("message", null) { it.textValue() }
        record.brandName = recordNode.getField("brand.name", null) { it.textValue() }
        record.senderDetails = recordNode.getField("senderDetails", null) { it.textValue() }
        record.authorizationId = recordNode.getTextValue("authorizationId")
        record.hasReceipt = recordNode.getBooleanValue("hasShoppingReceipt")

        return record
    }

}