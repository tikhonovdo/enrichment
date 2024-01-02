package ru.tikhonovdo.enrichment.service.file.worker

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.AbstractTestSuite
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.service.file.FileServiceTest
import ru.tikhonovdo.enrichment.service.importscenario.format
import java.nio.file.Paths
import java.time.LocalDateTime

class AlfabankFileWorkerTest : AbstractTestSuite() {
    @Autowired
    lateinit var draftTransactionRepository: DraftTransactionRepository

    private var DRAFT_NEXT_ID = 1L

    @Test
    fun stableOrderingTest() {
        val worker = AlfabankFileWorker(draftTransactionRepository)
        val sourceUrl = FileServiceTest::class.java.getResource("alfa/Statement 08.02.2023 - 03.04.2023.xlsx")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        worker.saveData(source.readBytes())

        val actual = draftTransactionRepository.findAll().onEach { draft ->
            draft.data = ""
        }
        Assertions.assertEquals(statementFebApr(), actual)
    }

    @Test
    fun alfaDeletionObsoleteTest() {
        val worker = AlfabankFileWorker(draftTransactionRepository)
        val sourceUrl = FileServiceTest::class.java.getResource("alfa/Statement 08.02.2023 - 03.04.2023.xlsx")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        worker.saveData(source.readBytes())

        val deleted = draftTransactionRepository.deleteObsoleteDraft()
        val actual = draftTransactionRepository.findAll().onEach { draft ->
            draft.data = ""
        }
        Assertions.assertEquals(0, deleted)
        Assertions.assertEquals(statementFebApr(), actual)
    }

    private fun statementFebApr() =
        listOf(
            draftOf("2023-04-03T00:00:03", "181.01"),
            draftOf("2023-04-03T00:00:02", "200.0"),
            draftOf("2023-04-03T00:00:01", "200.0"),
            draftOf("2023-04-03T00:00:00", "8.99"),
            draftOf("2023-03-24T00:00:03", "3041.01"),
            draftOf("2023-03-24T00:00:02", "3050.0"),
            draftOf("2023-03-24T00:00:01", "3050.0"),
            draftOf("2023-03-24T00:00:00", "5000.0"),
            draftOf("2023-02-11T00:00:02", "280.0"),
            draftOf("2023-02-11T00:00:01", "360.0"),
            draftOf("2023-02-11T00:00:00", "370.0"),
            draftOf("2023-02-08T00:00:02", "787.94"),
            draftOf("2023-02-08T00:00:01", "787.94"),
            draftOf("2023-02-08T00:00:00", "787.94")
        )

    private fun draftOf(rawDateTime: String, sum: String) = DraftTransaction(
        id = DRAFT_NEXT_ID++,
        bankId = Bank.ALFA.id,
        date = format.parse(rawDateTime, LocalDateTime::from),
        sum = sum, data = ""
    )

}