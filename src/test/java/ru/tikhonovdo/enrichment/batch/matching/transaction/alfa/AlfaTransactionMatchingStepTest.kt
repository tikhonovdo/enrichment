package ru.tikhonovdo.enrichment.batch.matching.transaction.alfa

import io.restassured.RestAssured.post
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository
import ru.tikhonovdo.enrichment.service.file.worker.AlfabankDataWorker
import java.nio.file.Paths

class AlfaTransactionMatchingStep(
    @Autowired private val draftTransactionRepository: DraftTransactionRepository,
    @Autowired private val transactionMatchingRepository: TransactionMatchingRepository
): DatabaseAwareTest() {

    @Test
    fun `filter out invalid transaction`() {
        val worker = AlfabankDataWorker(draftTransactionRepository)
        val sourceUrl = AlfaTransactionMatchingStepTest::class.java.getResource("Statement 03.01.2024 - 04.01.2024.xlsx")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()
        worker.saveData(source.readBytes())

        post("/matching?steps=alfaTransactionMatchingStep")
            .then()
            .assertThat()
            .body(equalTo("0 unmatched records left"))
            .statusCode(200)
        Assertions.assertEquals(listOf<TransactionMatching>(), transactionMatchingRepository.findAll())
    }

}