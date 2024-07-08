package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.io.IOException

class OperationsResponseDeserializer(vc: Class<*>?) : StdDeserializer<OperationsResponse>(vc) {

    constructor() : this(null)

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): OperationsResponse {
        val recordNode: JsonNode = parser.codec.readTree(parser)
        val operations = JsonMapper.JSON_MAPPER.readValue(
            recordNode.get("data").get("bankUser").get("operations").toString(),
            OperationsCollection::class.java
        )
        return OperationsResponse(operations)
    }

}