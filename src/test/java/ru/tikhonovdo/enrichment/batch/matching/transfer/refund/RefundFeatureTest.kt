package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.Account
import ru.tikhonovdo.enrichment.domain.enitity.Category
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.financepm.AccountRepository
import ru.tikhonovdo.enrichment.repository.financepm.CategoryRepository
import ru.tikhonovdo.enrichment.repository.financepm.CurrencyRepository
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class RefundFeatureTest(
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository,
    @Autowired private val transactionRepository: TransactionRepository,
    @Autowired private val currencyRepository: CurrencyRepository,
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val categoryRepository: CategoryRepository
): DatabaseAwareTest() {

    @BeforeEach
    fun beforeEach() {
        prefillData()
    }

    @Test
    fun `search refund happy path`() {
        jdbcTemplate.update("SET session_replication_role = replica")
        prepareDataForSearch()
        jdbcTemplate.update("SET session_replication_role = origin")

        RestAssured.post("/matching?steps=searchRefundStep")
            .then()
            .assertThat()
            .statusCode(200)

        val incomeTransaction = matchingTransactionRepository.findAll().associateBy { it.id }[3]!!
        Assertions.assertEquals(2L, incomeTransaction.refundForId)
        Assertions.assertEquals(false, incomeTransaction.validated)
    }

    @Test
    fun `apply refund happy path`(@Value("\${refund.income-category-id}") refundIncomeCategoryId: Long) {
        jdbcTemplate.update("SET session_replication_role = replica")
        prepareDataForApply(refundIncomeCategoryId)
        jdbcTemplate.update("SET session_replication_role = origin")

        RestAssured.post("/matching?steps=exportMatchingTransactionsStep,applyRefundStep")
            .then()
            .assertThat()
            .statusCode(200)

        val compensatedTransaction = transactionRepository.findById(2).orElseThrow()
        val incomeTransaction = matchingTransactionRepository.findById(3).orElseThrow()

        Assertions.assertEquals(refundIncomeCategoryId, compensatedTransaction.categoryId)
        Assertions.assertEquals(Type.INCOME.id, compensatedTransaction.typeId)
        Assertions.assertEquals(BigDecimal("76.000000"), compensatedTransaction.sum)
        Assertions.assertEquals(2L, incomeTransaction.refundForId)
        Assertions.assertEquals(true, incomeTransaction.validated)
    }

    private fun prepareDataForSearch() {
        matchingTransactionRepository.insertBatch(outcomeTransactions() +
            TransactionMatching(
                id = 3,
                name = "Перевод",
                typeId = 1,
                categoryId = null,
                date = LocalDateTime.of(2023, 11, 6, 15, 54, 57),
                sum = BigDecimal("200.0"),
                accountId = 1L,
                description = "ЛюдиЛюбят"
            )
        )
    }

    private fun prepareDataForApply(refundIncomeCategoryId: Long) {
        categoryRepository.insertBatch(listOf(
            Category(id = refundIncomeCategoryId, name = "refund_income_category", typeId = Type.INCOME.id, orderId = 2),
        ))
        matchingTransactionRepository.insertBatch(outcomeTransactions() + listOf(TransactionMatching(
            id = 3,
            name = "Перевод",
            typeId = 1,
            categoryId = null,
            date = LocalDateTime.of(2023, 11, 6, 15, 54, 57),
            sum = BigDecimal("200.0"),
            accountId = 1L,
            description = "ЛюдиЛюбят",
            refundForId = 2L
        )))
    }

    private fun outcomeTransactions() = listOf(
        TransactionMatching(
            id = 1,
            name = "ЛюдиЛюбят",
            typeId = Type.OUTCOME.id,
            categoryId = 1,
            date = LocalDateTime.of(2023, 11, 6, 15, 52, 56),
            sum = BigDecimal("81.0"),
            accountId = 1L,
        ),
        TransactionMatching(
            id = 2,
            name = "ЛюдиЛюбят",
            typeId = Type.OUTCOME.id,
            categoryId = 1,
            date = LocalDateTime.of(2023, 11, 6, 15, 54, 42),
            sum = BigDecimal("124.0"),
            accountId = 1L,
        )
    )

    private fun prefillData() {
        matchingTransactionRepository.updateSequence()
        transactionRepository.updateSequence()
        currencyRepository.updateSequence()
        accountRepository.updateSequence()
        categoryRepository.updateSequence()
        currencyRepository.insertBatch(listOf(Currency(id = 1L, name = "RUB")))
        accountRepository.insertBatch(listOf(Account(id = 1L, name = "test_account", currencyId = 1L, orderId = 1)))
        categoryRepository.insertBatch(listOf(
            Category(id = 1, name = "outcome_category", typeId = Type.OUTCOME.id, orderId = 1),
            Category(id = 2, name = "income_category", typeId = Type.INCOME.id, orderId = 1),
        ))
    }

}