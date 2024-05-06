package ru.tikhonovdo.enrichment.batch.matching.transfer

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
import ru.tikhonovdo.enrichment.repository.financepm.*
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository
import java.math.BigDecimal
import java.time.LocalDateTime

class TransferMatchingTest(
    @Autowired private val draftTransactionRepository: DraftTransactionRepository,
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository,
    @Autowired private val transferMatchingRepository: TransferMatchingRepository,
    @Autowired private val transactionRepository: TransactionRepository,
    @Autowired private val transferRepository: TransferRepository,
    @Autowired private val currencyRepository: CurrencyRepository,
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val accountMatchingRepository: AccountMatchingRepository,
    @Autowired private val categoryRepository: CategoryRepository
): DatabaseAwareTest() {

    lateinit var accounts: Map<Long, Account>

    @BeforeEach
    fun beforeEach() {
        prefillData()
        accounts = accountRepository.findAll().associateBy { it.id!! }
    }

    @Test
    fun `transferMatchingStep - matching transfer after transferComplementStep (cash out, name pattern partial match)`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_complement " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('MCC6011', '', 2, 1, 3)")
        transactionMatching(
            name = "Alfa Iss 10.01.24 09.01.24 10000.00 RUR MCC6011",
            type = Type.OUTCOME,
            sum = BigDecimal("10000.000000"),
            accountId = 1,
            description = ""
        )

        RestAssured.post("/matching?steps=transferComplementStep,transferMatchingStep").then().assertThat().statusCode(200)

        val alfaCashOut = matchingTransactionRepository.findAll().associateBy { accounts[it.accountId]!!.name }
        val transfers = transferMatchingRepository.findAll()
        Assertions.assertEquals(1, transfers.size)

        val transfer = transfers.first()
        Assertions.assertEquals(transfer.matchingTransactionIdFrom, alfaCashOut["alfa_account"]!!.id)
        Assertions.assertEquals(transfer.matchingTransactionIdTo, alfaCashOut["cash_account"]!!.id)
    }

    @Test
    fun `transferMatchingStep - matching transfer after transferComplementStep (outgoing based self-transfer)`() {
        jdbcTemplate.update("INSERT INTO matching.transfer_complement " +
                "(source_name, source_description, source_type, source_account_id, target_account_id) " +
                "VALUES ('Перевод по запросу самому себе', 'Яндекс Банк', 2, 2, 4)")
        transactionMatching(
            name = "Перевод по запросу самому себе",
            type = Type.OUTCOME,
            sum = BigDecimal("4321.000000"),
            accountId = 2,
            description = "Яндекс Банк",
        )

        RestAssured.post("/matching?steps=transferComplementStep,transferMatchingStep").then().assertThat().statusCode(200)

        val tinkoffToYa = matchingTransactionRepository.findAll().associateBy { accounts[it.accountId]!!.name }
        val transfers = transferMatchingRepository.findAll()
        Assertions.assertEquals(1, transfers.size)

        val transfer = transfers.first()
        Assertions.assertEquals(transfer.matchingTransactionIdFrom, tinkoffToYa["tinkoff_account"]!!.id)
        Assertions.assertEquals(transfer.matchingTransactionIdTo, tinkoffToYa["ya_account"]!!.id)
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
        draftTransactionRepository.insertBatch(listOf(
            DraftTransaction(
                1,
                Bank.TINKOFF.id,
                LocalDateTime.now(),
                "0.00",
                "{}"
            )
        ))
    }

    private fun transactionMatching(name: String, type: Type, sum: BigDecimal, accountId: Long, description: String) {
        matchingTransactionRepository.insertBatch(listOf(
            TransactionMatching(
                name = name,
                typeId = type.id,
                date = LocalDateTime.now(),
                sum = sum,
                accountId = accountId,
                description = description,
                draftTransactionId = 1
            )
        ))
    }

}