package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsDataPayload
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.nio.file.Paths

class TinkoffAdditionalRawOperationDataParseTest {

    @Test
    fun baseParseTest() {
        val sourceUrl = TinkoffAdditionalRawOperationDataParseTest::class.java.getResource("operations-payload.json")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        val payload = JsonMapper.JSON_MAPPER.readValue(source, TinkoffOperationsDataPayload::class.java)

        Assertions.assertEquals("OK", payload.resultCode)
    }

}