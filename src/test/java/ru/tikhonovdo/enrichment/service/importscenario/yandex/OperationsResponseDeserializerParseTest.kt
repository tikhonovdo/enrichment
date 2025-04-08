package ru.tikhonovdo.enrichment.service.importscenario.yandex

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransaction
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransactionFeedResponse
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.nio.file.Paths

class OperationsResponseDeserializerParseTest {

    @Test
    fun baseParseTest() {
        val sourceUrl = OperationsResponseDeserializerParseTest::class.java.getResource("operations-payload.json")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        val response = JsonMapper.JSON_MAPPER.readValue(source, YaTransactionFeedResponse::class.java)

        val serializedOperations = JsonMapper.JSON_MAPPER.writeValueAsString(response.items).toByteArray()
        val deserializedOperations = JsonMapper.JSON_MAPPER.readValue(String(serializedOperations), ListYaTransactionRef())

        Assertions.assertEquals("cursor", response.cursor)
    }

}

class ListYaTransactionRef: TypeReference<List<YaTransaction>>()