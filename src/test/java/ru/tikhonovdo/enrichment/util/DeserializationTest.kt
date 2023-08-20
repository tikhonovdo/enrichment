package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import java.math.BigDecimal
import java.time.LocalDateTime

@RunWith(SpringJUnit4ClassRunner::class)
class DeserializationTest {
    private var enrichmentJsonMapper: ObjectMapper = JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .build()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setDateFormat(StdDateFormat())

    @Test
    @Throws(JsonProcessingException::class)
    fun deserializeTransactionTest() {
        val json: String = """
            {"id": "4",
            "name": "Перевод",
            "type": 2,
            "categoryId": 0,
            "date": "1405900800000",
            "sum": "1400.0",
            "accountId": 1,
            "description": "",
            "source": 1,
            "available": 1}
        """.trimIndent()
        val expected = Transaction(
            4L,
            "Перевод",
            2,
            null,
            LocalDateTime.of(2014, 7, 21, 4, 0),
            BigDecimal.valueOf(1400.0),
            1L,
            "",
            1L,
            null,
            true,
            null,
            null,
            null,
            null
        )

        val actual = enrichmentJsonMapper.readValue(json, Transaction::class.java)

        Assert.assertEquals(expected, actual)
    }
}