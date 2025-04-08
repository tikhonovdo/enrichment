package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.io.IOException

class YaTransactionFeedResponseDeserializer(vc: Class<*>?) : StdDeserializer<YaTransactionFeedResponse>(vc) {

    constructor() : this(null)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): YaTransactionFeedResponse {
        val recordNode: JsonNode = parser.codec.readTree(parser)
        val data = recordNode.get("data").get("getTransactionsFeedView")
        val cursor = data.get("cursor").textValue()
        val arrayNode = data.get("items")
        val items: List<YaTransaction> = JsonMapper.JSON_MAPPER.readerForListOf(YaTransaction::class.java).readValue(arrayNode)
        return YaTransactionFeedResponse(items, cursor);
    }

}