package ru.tikhonovdo.enrichment.batch.matching.transfer.manual

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
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CurrencyMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TransferManualMatchingStepTest(
    @Autowired private val draftTransactionRepository: DraftTransactionRepository,
    @Autowired private val matchingTransactionRepository: TransactionMatchingRepository,
    @Autowired private val currencyRepository: CurrencyRepository,
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val accountMatchingRepository: AccountMatchingRepository,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val transferMatchingRepository: TransferMatchingRepository,
    @Autowired private val currencyMatchingRepository: CurrencyMatchingRepository
) : DatabaseAwareTest() {

    @BeforeEach
    fun beforeEach() {
        prefillData()
    }

    @Test
    fun `transferManualMatchingStep - happy path by different dates`() {
        jdbcTemplate.update(
            "INSERT INTO matching.transfer_manual " +
                    "(source_name, source_description, source_account_id, target_name, target_description, target_account_id) " +
                    "VALUES ('Иван Иванович И', '', 1, 'Иван И.', '', 2)"
        )
        draftTransactionRepository.insertBatch(
            listOf(
                DraftTransaction(
                    id = 1, bankId = Bank.ALFA.id, date = LocalDate.of(2023, 12, 11).atStartOfDay(), sum = "5000.0",
                    data = "{\"mcc\": null, \"type\": \"Списание\", \"status\": \"Выполнен\", \"comment\": \"\", \"cardName\": \"\", \"category\": \"Прочие расходы\", \"cardNumber\": \"\", \"paymentSum\": 5000.0, \"accountName\": \"Текущий счёт\", \"description\": \"Иван Иванович И\", \"paymentDate\": \"11.12.2023\", \"accountNumber\": \"A321\", \"operationDate\": \"11.12.2023\", \"paymentCurrency\": \"RUR\"}"
                ),
                DraftTransaction(
                    id = 2, bankId = Bank.TINKOFF.id, date = LocalDateTime.of(2023, 12, 11, 21, 39, 46), sum = "5000.0",
                    data = "{\"mcc\": null, \"status\": \"OK\", \"message\": null, \"cashback\": null, \"category\": \"Переводы\", \"brandName\": \"Альфа-Банк\", \"cardNumber\": \"*123\", \"paymentSum\": 5000.0, \"description\": \"Иван И.\", \"paymentDate\": \"11.12.2023\", \"operationSum\": 5000.0, \"totalBonuses\": 0.0, \"accountNumber\": \"T123\", \"operationDate\": \"11.12.2023 21:39:46\", \"paymentCurrency\": \"RUB\", \"operationCurrency\": \"RUB\", \"roundingForInvestKopilka\": 0.0, \"sumWithRoundingForInvestKopilka\": 5000.0}"
                ),
                DraftTransaction(
                    id = 3, bankId = Bank.ALFA.id, date = LocalDate.of(2023, 12, 12).atStartOfDay(), sum = "2000.0",
                    data = "{\"mcc\": null, \"type\": \"Списание\", \"status\": \"Выполнен\", \"comment\": \"\", \"cardName\": \"\", \"category\": \"Прочие расходы\", \"cardNumber\": \"\", \"paymentSum\": 2000.0, \"accountName\": \"Текущий счёт\", \"description\": \"Иван Иванович И\", \"paymentDate\": \"12.12.2023\", \"accountNumber\": \"A321\", \"operationDate\": \"12.12.2023\", \"paymentCurrency\": \"RUR\"}"
                ),
                DraftTransaction(
                    id = 4, bankId = Bank.TINKOFF.id, date = LocalDateTime.of(2023, 12, 12, 17, 4, 8), sum = "2000.0",
                    data = "{\"mcc\": null, \"status\": \"OK\", \"message\": null, \"cashback\": null, \"category\": \"Переводы\", \"brandName\": \"Альфа-Банк\", \"cardNumber\": \"*123\", \"paymentSum\": 2000.0, \"description\": \"Иван И.\", \"paymentDate\": \"12.12.2023\", \"operationSum\": 2000.0, \"totalBonuses\": 0.0, \"accountNumber\": \"T123\", \"operationDate\": \"12.12.2023 17:04:08\", \"paymentCurrency\": \"RUB\", \"operationCurrency\": \"RUB\", \"roundingForInvestKopilka\": 0.0, \"sumWithRoundingForInvestKopilka\": 2000.0}"
                ),
                DraftTransaction(
                    id = 5, bankId = Bank.ALFA.id, date = LocalDateTime.of(2023, 12, 13, 0, 0, 3), sum = "3000.0",
                    data = "{\"mcc\": null, \"type\": \"Списание\", \"status\": \"Выполнен\", \"comment\": \"\", \"cardName\": \"\", \"category\": \"Прочие расходы\", \"cardNumber\": \"\", \"paymentSum\": 3000.0, \"accountName\": \"Текущий счёт\", \"description\": \"Иван Иванович И\", \"paymentDate\": \"13.12.2023\", \"accountNumber\": \"A321\", \"operationDate\": \"13.12.2023\", \"paymentCurrency\": \"RUR\"}"
                ),
                DraftTransaction(
                    id = 6, bankId = Bank.TINKOFF.id, date = LocalDateTime.of(2023, 12, 13, 18, 2, 20), sum = "3000.0",
                    data = "{\"mcc\": null, \"status\": \"OK\", \"message\": \"Перевод по СБП\", \"cashback\": null, \"category\": \"Переводы\", \"brandName\": \"Альфа-Банк\", \"cardNumber\": \"*123\", \"paymentSum\": 3000.0, \"description\": \"Иван И.\", \"paymentDate\": \"13.12.2023\", \"operationSum\": 3000.0, \"totalBonuses\": 0.0, \"accountNumber\": \"T123\", \"operationDate\": \"13.12.2023 18:02:20\", \"paymentCurrency\": \"RUB\", \"operationCurrency\": \"RUB\", \"roundingForInvestKopilka\": 0.0, \"sumWithRoundingForInvestKopilka\": 3000.0}"
                )
            )
        )

        RestAssured.post("/matching").then().assertThat().statusCode(200)

        val matchingTransactions = matchingTransactionRepository.findAll()
        val mtIdByDtId = matchingTransactions.associateBy({ it.draftTransactionId }, { it.id })
        val transfers = transferMatchingRepository.findAll()
        transfers.sortBy { it.matchingTransactionIdFrom }
        Assertions.assertEquals(6, matchingTransactions.filter { it.eventId == 1L }.size)
        Assertions.assertEquals(mtIdByDtId[1L] to mtIdByDtId[2L], transfers[0].matchingTransactionIdFrom to transfers[0].matchingTransactionIdTo)
        Assertions.assertEquals(mtIdByDtId[3L] to mtIdByDtId[4L], transfers[1].matchingTransactionIdFrom to transfers[1].matchingTransactionIdTo)
        Assertions.assertEquals(mtIdByDtId[5L] to mtIdByDtId[6L], transfers[2].matchingTransactionIdFrom to transfers[2].matchingTransactionIdTo)
    }

    @Test
    fun `transfer-like transactions without specified categories will be matched for only earliest transaction in time order`() {
        val transactions = listOf(
            TransactionMatching(id = 1, name = "Бабагануш", typeId = 2, date = LocalDateTime.parse("2025-08-31T12:19:39.745000"), sum = BigDecimal(720.0),accountId = 4, description = "Расход"),
            TransactionMatching(id = 2, name = "Дмитрий Т.", typeId = 1, date = LocalDateTime.parse("2025-08-31T12:19:39.137000"),sum = BigDecimal(720.0),accountId = 4, description = "Тинькофф"),
            TransactionMatching(id = 3, name = "Перевод по запросу самому себе",typeId = 2, date = LocalDateTime.parse("2025-08-31T12:19:35.000000"),sum = BigDecimal(720.0), accountId = 2, description = "Яндекс"),
            TransactionMatching(id = 4, name = "EREMEEV K.V,",typeId = 2, date = LocalDateTime.parse("2025-08-31T12:16:53.208000"),sum = BigDecimal(1140.0),accountId = 4, description = "Расход"),
            TransactionMatching(id = 5, name = "Дмитрий Т.",typeId = 1, date = LocalDateTime.parse("2025-08-31T12:16:52.642000"),sum = BigDecimal(1140.0),accountId = 4, description = "Тинькофф"),
            TransactionMatching(id = 6, name = "Перевод по запросу самому себе",typeId = 2, date = LocalDateTime.parse("2025-08-31T12:16:49.000000"),sum = BigDecimal(1140.0), accountId = 2 , description = "Яндекс")
        )
        matchingTransactionRepository.insertBatch(transactions)

        RestAssured.post("/matching?includedSteps=transferMatchingStep").then().assertThat().statusCode(200)

        val matchingTransactions = matchingTransactionRepository.findAll()
        val transfers = transferMatchingRepository.findAll()
        transfers.sortBy { it.matchingTransactionIdFrom }
        Assertions.assertEquals(4, matchingTransactions.filter { it.eventId == 1L }.size)
        Assertions.assertEquals(3, transfers[0].matchingTransactionIdFrom)
        Assertions.assertEquals(2, transfers[0].matchingTransactionIdTo)
        Assertions.assertEquals(6, transfers[1].matchingTransactionIdFrom)
        Assertions.assertEquals(5, transfers[1].matchingTransactionIdTo)
    }

    private fun prefillData() {
        jdbcTemplate.update("SET session_replication_role = replica")
        matchingTransactionRepository.updateSequence()
        currencyRepository.updateSequence()
        currencyRepository.insertBatch(listOf(Currency(id = 1L, name = "RUB")))
        currencyMatchingRepository.insertBatch(
            listOf(
                CurrencyMatching(currencyId = 1L, bankId = Bank.ALFA.id, bankCurrencyCode = "RUR"),
                CurrencyMatching(currencyId = 1L, bankId = Bank.TINKOFF.id, bankCurrencyCode = "RUB"),
            )
        )
        accountMatchingRepository.insertBatch(
            listOf(
                AccountMatching(1L, bankId = Bank.ALFA.id, "A321"),
                AccountMatching(2L, bankId = Bank.TINKOFF.id, "T123")
            )
        )
        accountRepository.updateSequence()
        accountRepository.insertBatch(
            listOf(
                Account(id = 1L, name = "alfa_account", currencyId = 1L, orderId = 1),
                Account(id = 2L, name = "tinkoff_account", currencyId = 1L, orderId = 2),
                Account(id = 3L, name = "cash_account", currencyId = 1L, orderId = 3),
                Account(id = 4L, name = "ya_account", currencyId = 1L, orderId = 4),
            )
        )
        categoryRepository.updateSequence()
        categoryRepository.insertBatch(
            listOf(
                Category(id = 1, name = "income_category", typeId = Type.INCOME.id, orderId = 1),
                Category(id = 2, name = "outcome_category", typeId = Type.OUTCOME.id, orderId = 2)
            )
        )
        jdbcTemplate.execute("SELECT setval('matching.draft_transaction_id_seq', (SELECT coalesce(MAX(id) + 1, 1) FROM matching.draft_transaction), false)")
        jdbcTemplate.update("SET session_replication_role = origin")
    }

}