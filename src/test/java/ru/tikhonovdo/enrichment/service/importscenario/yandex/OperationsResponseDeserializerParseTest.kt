package ru.tikhonovdo.enrichment.service.importscenario.yandex

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaOperationsResponse
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.nio.file.Paths

class OperationsResponseDeserializerParseTest {

    @Test
    fun baseParseTest() {
        val sourceUrl = OperationsResponseDeserializerParseTest::class.java.getResource("operations-payload.json")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        val response = JsonMapper.JSON_MAPPER.readValue(source, YaOperationsResponse::class.java)

        Assertions.assertEquals("cursor", response.operations.cursor)
    }

}