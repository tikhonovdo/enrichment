package ru.tikhonovdo.enrichment.batch.matching.transfer.pattern

import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.financepm.AccountRepository
import ru.tikhonovdo.enrichment.repository.financepm.CategoryRepository
import ru.tikhonovdo.enrichment.repository.financepm.CurrencyRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class TransferPatternPreMatchingTest(
    @Autowired private val draftTransactionRepository: DraftTransactionRepository,
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository,
    @Autowired private val currencyRepository: CurrencyRepository,
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val categoryRepository: CategoryRepository
): DatabaseAwareTest() {

    @BeforeEach
    fun beforeEach() {
        prefillData()
    }

    @Test
    fun `transferPatternPreMatchingStep - cash out, name pattern partial match`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_pattern " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('MCC6011', '', 2, 1, 3)")
        transactionMatching(
            name = "Alfa Iss 10.01.24 09.01.24 10000.00 RUR MCC6011",
            type = Type.OUTCOME,
            sum = BigDecimal("10000.000000"),
            accountId = 1,
            description = ""
        )

        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)

        val alfaCashOut = matchingTransactionRepository.findAll().sortedByDescending { it.typeId }
        Assertions.assertEquals(2, alfaCashOut.size)
        Assertions.assertEquals(1, alfaCashOut[0].accountId)
        Assertions.assertEquals(3, alfaCashOut[1].accountId)
        Assertions.assertEquals(Type.OUTCOME.id, alfaCashOut[0].typeId)
        Assertions.assertEquals(Type.INCOME.id, alfaCashOut[1].typeId)
        Assertions.assertEquals(alfaCashOut[0].sum, alfaCashOut[1].sum)
        Assertions.assertEquals(alfaCashOut[0].date, alfaCashOut[1].date)
    }

    @Test
    fun `transferPatternPreMatchingStep - cash out, name pattern full match`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_pattern " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('Снятие в банкомате', '', 2, 2, 3)")
        transactionMatching(
            name = "Снятие в банкомате",
            type = Type.OUTCOME,
            sum = BigDecimal("5000.000000"),
            accountId = 2,
            description = "",
        )

        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)

        val tinkoffCashOut = matchingTransactionRepository.findAll().sortedByDescending { it.typeId }
        Assertions.assertEquals(2, tinkoffCashOut.size)
        Assertions.assertEquals(2, tinkoffCashOut[0].accountId)
        Assertions.assertEquals(3, tinkoffCashOut[1].accountId)
        Assertions.assertEquals(Type.OUTCOME.id, tinkoffCashOut[0].typeId)
        Assertions.assertEquals(Type.INCOME.id, tinkoffCashOut[1].typeId)
        Assertions.assertEquals(tinkoffCashOut[0].sum, tinkoffCashOut[1].sum)
        Assertions.assertEquals(tinkoffCashOut[0].date, tinkoffCashOut[1].date)
    }

    @Test
    fun `transferPatternPreMatchingStep - incoming based self-transfer`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_pattern " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('Перевод по запросу самому себе', 'Яндекс Банк', 1, 2, 4)")
        transactionMatching(
            name = "Перевод по запросу самому себе",
            type = Type.INCOME,
            sum = BigDecimal("1234.000000"),
            accountId = 2,
            description = "Яндекс Банк",
        )

        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)

        val yaToTinkoff = matchingTransactionRepository.findAll().sortedByDescending { it.typeId }
        Assertions.assertEquals(2, yaToTinkoff.size)
        Assertions.assertEquals(4, yaToTinkoff[0].accountId)
        Assertions.assertEquals(2, yaToTinkoff[1].accountId)
        Assertions.assertEquals(Type.OUTCOME.id, yaToTinkoff[0].typeId)
        Assertions.assertEquals(Type.INCOME.id, yaToTinkoff[1].typeId)
        Assertions.assertEquals(yaToTinkoff[0].sum, yaToTinkoff[1].sum)
        Assertions.assertEquals(yaToTinkoff[0].date, yaToTinkoff[1].date)
    }

    @Test
    fun `transferPatternPreMatchingStep - outgoing based self-transfer`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_pattern " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('Перевод по запросу самому себе', 'Яндекс Банк', 2, 2, 4)")
        transactionMatching(
            name = "Перевод по запросу самому себе",
            type = Type.OUTCOME,
            sum = BigDecimal("4321.000000"),
            accountId = 2,
            description = "Яндекс Банк",
        )

        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)

        val tinkoffToYa = matchingTransactionRepository.findAll().sortedByDescending { it.typeId }
        Assertions.assertEquals(2, tinkoffToYa.size)
        Assertions.assertEquals(2, tinkoffToYa[0].accountId)
        Assertions.assertEquals(4, tinkoffToYa[1].accountId)
        Assertions.assertEquals(Type.OUTCOME.id, tinkoffToYa[0].typeId)
        Assertions.assertEquals(Type.INCOME.id, tinkoffToYa[1].typeId)
        Assertions.assertEquals(tinkoffToYa[0].sum, tinkoffToYa[1].sum)
    }

    @Test
    fun `transferPatternPreMatchingStep - idempotency test`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_pattern " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('MCC6011', '', 2, 1, 3)")
        transactionMatching(
            name = "Alfa Iss 10.01.24 09.01.24 10000.00 RUR MCC6011",
            type = Type.OUTCOME,
            sum = BigDecimal("10000.000000"),
            accountId = 1,
            description = ""
        )

        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)
        RestAssured.post("/matching?steps=transferPatternPreMatchingStep").then().assertThat().statusCode(200)

        val alfaCashOut = matchingTransactionRepository.findAll()
        Assertions.assertEquals(2, alfaCashOut.size)
    }

    private fun prefillData() {
        matchingTransactionRepository.updateSequence()
        currencyRepository.updateSequence()
        currencyRepository.insertBatch(listOf(Currency(id = 1L, name = "RUB")))
        accountRepository.updateSequence()
        accountRepository.insertBatch(listOf(
            Account(id = 1L, name = "alfa_account", currencyId = 1L, orderId = 1),
            Account(id = 2L, name = "tinkoff_account", currencyId = 1L, orderId = 2),
            Account(id = 3L, name = "cash_account", currencyId = 1L, orderId = 3),
            Account(id = 4L, name = "ya_account", currencyId = 1L, orderId = 4),
        ))
        categoryRepository.updateSequence()
        categoryRepository.insertBatch(listOf(
            Category(id = 1, name = "income_category", typeId = Type.INCOME.id, orderId = 1),
            Category(id = 2, name = "outcome_category", typeId = Type.OUTCOME.id, orderId = 2)
        ))
        jdbcTemplate.execute("SELECT setval('matching.draft_transaction_id_seq', (SELECT coalesce(MAX(id) + 1, 1) FROM matching.draft_transaction), false)")
        draftTransactionRepository.insertBatch(listOf(DraftTransaction(1, Bank.TINKOFF.id, LocalDateTime.now(), "0.00", "{}")))
    }

    private fun transactionMatching(name: String, type: Type, sum: BigDecimal, accountId: Long, description: String) {
        matchingTransactionRepository.insertBatch(listOf(TransactionMatching(
            name = name,
            typeId = type.id,
            date = LocalDateTime.now(),
            sum = sum,
            accountId = accountId,
            description = description,
            draftTransactionId = 1
        )))
    }

}