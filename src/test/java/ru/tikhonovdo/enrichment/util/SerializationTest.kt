package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.core.JsonProcessingException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.math.BigDecimal
import java.time.LocalDateTime

@RunWith(SpringJUnit4ClassRunner::class)
class SerializationTest {
    @Test
    @Throws(JsonProcessingException::class)
    fun serializeTransactionTest() {
        val transaction = Transaction(
            id = 4L,
            name = "Перевод",
            typeId = 2,
            categoryId = null,
            date = LocalDateTime.of(2014, 7, 21, 4, 0),
            sum = BigDecimal.valueOf(1400.0),
            accountId = 1L,
            description = "",
            eventId = null,
            available = true
        )
        val expected: String = """
            {"id":"4",
            "name":"Перевод",
            "type":2,
            "categoryId":0,
            "date":"1405900800000",
            "sum":"1400.0",
            "accountId":1,
            "description":"",
            "source":0,
            "available":1}
        """.trimIndent().replace("\n", "")

        val actual = JSON_MAPPER.writeValueAsString(transaction)

        Assert.assertEquals(expected, actual)
    }
}