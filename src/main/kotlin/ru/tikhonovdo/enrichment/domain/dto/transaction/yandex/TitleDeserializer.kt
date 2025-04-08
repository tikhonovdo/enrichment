package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException

class TitleDeserializer(vc: Class<*>? = null) : StdDeserializer<String>(vc) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): String {
        val recordNode: JsonNode = parser.codec.readTree(parser)

        return recordNode.get("plain")?.textValue() ?: recordNode.textValue()
    }

}