package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import io.restassured.RestAssured.*
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.AbstractTestSuite
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class CleanUnmatchedTransactionsStepTest(
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository
): AbstractTestSuite() {

    @BeforeEach
    fun beforeEach() {
        prepareData()
    }

    @Test
    fun `happy path`() {
        var unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()
        Assertions.assertEquals(setOf(1L, 2L), unmatchedIds)

        post("/matching?steps=cleanUnmatchedTransactionsStep")
            .then()
            .assertThat()
            .body(equalTo("0 unmatched records left"))
            .statusCode(200)

        val count = matchingTransactionRepository.count()
        Assertions.assertEquals(0, count)

        matchingTransactionRepository.insertBatch(listOf(createInvalidTransaction("transaction 3")))
        unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()

        Assertions.assertEquals(setOf(1L), unmatchedIds)
    }

    fun prepareData() {
        matchingTransactionRepository.insertBatch(listOf(
            createInvalidTransaction("transaction 1"),
            createInvalidTransaction("transaction 2")
        ))
    }

    fun createInvalidTransaction(name: String) =
        TransactionMatching(
            name = name,
            typeId = 1,
            categoryId = null,
            date = LocalDateTime.now(),
            sum = BigDecimal("100.00"),
            accountId = null
        )
}