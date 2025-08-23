package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import io.restassured.RestAssured.get
import io.restassured.RestAssured.post
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.Account
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.financepm.AccountRepository
import ru.tikhonovdo.enrichment.repository.financepm.CurrencyRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class CleanUnmatchedTransactionsStepTest(
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository,
    @Autowired private val currencyRepository: CurrencyRepository,
    @Autowired private val accountRepository: AccountRepository
): DatabaseAwareTest() {

    @Test
    fun `transactions without accounts are invalid and wiped`() {
        prepareHappyPathData()

        var unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()
        Assertions.assertEquals(setOf(1L, 2L), unmatchedIds)

        post("/matching?steps=cleanUnmatchedTransactionsStep")
            .then()
            .assertThat()
            .statusCode(200)
        get("/matching/count")
            .then()
            .assertThat()
            .body(equalTo("0"))

        val count = matchingTransactionRepository.count()
        Assertions.assertEquals(0, count)

        matchingTransactionRepository.insertBatch(listOf(createInvalidTransaction("transaction 3")))
        unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()

        Assertions.assertEquals(setOf(1L), unmatchedIds)
    }

    @Test
    fun `valid transfer transactions are not count as unmatched`() {
        prepareTransfersPathData()

        var unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()
        Assertions.assertEquals(0, unmatchedIds.size)

        post("/matching?steps=cleanUnmatchedTransactionsStep")
            .then()
            .assertThat()
            .statusCode(200)
        get("/matching/count")
            .then()
            .assertThat()
            .body(equalTo("0"))

        matchingTransactionRepository.insertBatch(listOf(createInvalidTransaction("transaction 3")))
        unmatchedIds = matchingTransactionRepository.getUnmatchedTransactionIds().toSet()

        Assertions.assertEquals(setOf(3L), unmatchedIds)
    }

    fun prepareHappyPathData() {
        matchingTransactionRepository.insertBatch(listOf(
            createInvalidTransaction("transaction 1"),
            createInvalidTransaction("transaction 2")
        ))
    }

    fun prepareTransfersPathData(validated: Boolean = false) {
        currencyRepository.insertBatch(listOf(
            Currency(1,"Российский рубль","RUB",2,true)
        ))
        accountRepository.insertBatch(listOf(
            createAccount(1, "test_account out", currencyId = 1),
            createAccount(2, "test_account in", currencyId = 1)
        ))
        matchingTransactionRepository.insertBatch(listOf(
            createTransferTransaction("transfer #1", Type.OUTCOME, validated),
            createTransferTransaction("transfer #1", Type.INCOME, validated)
        ))
    }

    fun createInvalidTransaction(name: String) = createTransaction(name)

    fun createTransferTransaction(name: String, type: Type, validated: Boolean) =
        createTransaction(name, 1L, type, null, 1L, validated)

    fun createTransaction(name: String,
                          accountId : Long? = null,
                          type: Type = Type.INCOME,
                          categoryId: Long? = null,
                          eventId : Long? = null,
                          validated: Boolean = false
    ) = TransactionMatching(
        name = name,
        typeId = type.id,
        categoryId = categoryId,
        date = LocalDateTime.now(),
        sum = BigDecimal("100.00"),
        accountId = accountId,
        eventId = eventId,
        validated = validated
    )

    fun createAccount(id: Long, name: String, currencyId: Long) =
        Account(id, name, currencyId = currencyId, orderId = 1)

}