package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsAdditionalDataPayload
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.nio.file.Paths

class TinkoffAdditionalOperationDataParseTest {

    @Test
    fun baseParseTest() {
        val sourceUrl = TinkoffAdditionalOperationDataParseTest::class.java.getResource("operations-payload.json")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        val payload = JsonMapper.JSON_MAPPER.readValue(source, TinkoffOperationsAdditionalDataPayload::class.java)

        Assertions.assertEquals("OK", payload.resultCode)
    }

}