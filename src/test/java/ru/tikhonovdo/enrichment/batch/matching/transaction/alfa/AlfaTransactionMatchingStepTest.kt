package ru.tikhonovdo.enrichment.batch.matching.transaction.alfa

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository

//todo: нужно переписать заново
class AlfaTransactionMatchingStepTest(
    @Autowired private val draftTransactionRepository: DraftTransactionRepository,
    @Autowired private val transactionMatchingRepository: TransactionMatchingRepository,
    @Autowired private val transferMatchingRepository: TransferMatchingRepository,
): DatabaseAwareTest() {

    @Test
    fun `filter out invalid transaction`() {
//        val worker = AlfabankDataWorker(draftTransactionRepository)
//        val sourceUrl = AlfaTransactionMatchingStepTest::class.java.getResource("Statement 03.01.2024 - 04.01.2024.xlsx")
//        val source = Paths.get(sourceUrl!!.toURI()).toFile()
//        val drafts = worker.toDraftTransactionList(source.readBytes())
//        worker.saveDrafts(drafts)
//
//        post("/matching?steps=alfaTransactionMatchingStep")
//            .then()
//            .assertThat()
//            .body(equalTo("0 unmatched records left"))
//            .statusCode(200)
//        Assertions.assertEquals(listOf<TransactionMatching>(), transactionMatchingRepository.findAll())
    }

//    @Test
//    fun `alfa cash transfer happy path`() {
//        val worker = AlfabankDataWorker(draftTransactionRepository)
//        val sourceUrl = AlfaTransactionMatchingStepTest::class.java.getResource("Statement 1.01.2024 - 14.01.2024.xlsx")
//        val source = Paths.get(sourceUrl!!.toURI()).toFile()
//        worker.saveData(source.readBytes())
//
//        post("/matching?steps=alfaTransactionMatchingStep,alfaCashTransferMatchingStep")
//            .then()
//            .assertThat()
//            .body(equalTo("0 unmatched records left"))
//            .statusCode(200)
//        Assertions.assertEquals(2, transactionMatchingRepository.findAll().count())
//        Assertions.assertEquals(1, transferMatchingRepository.findAll().count())
//    }

}