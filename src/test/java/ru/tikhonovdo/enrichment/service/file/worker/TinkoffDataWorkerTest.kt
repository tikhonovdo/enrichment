package ru.tikhonovdo.enrichment.service.file.worker

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.tikhonovdo.enrichment.DatabaseAwareTest
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TinkoffDataWorkerTest : DatabaseAwareTest() {

    @Autowired
    lateinit var draftTransactionRepository: DraftTransactionRepository

    @Test
    fun deduplicationTest() {
        val worker = TinkoffDataWorker(draftTransactionRepository)
        val additionalDataUrl = TinkoffDataWorkerTest::class.java.getResource("../tinkoff/deduplication/additional_data.json")
        val additionalData = Paths.get(additionalDataUrl!!.toURI()).toFile()
        val sourceUrl = TinkoffDataWorkerTest::class.java.getResource("../tinkoff/deduplication/operations Sat Jun 15 12_34_56 MSK 2024-Sat Jun 15 12_34_56 MSK 2024.xls")
        val source = Paths.get(sourceUrl!!.toURI()).toFile()

        worker.saveData(source.readBytes(), additionalData.readBytes())

        val actual =  draftTransactionRepository.findAll().map { it.copy(data = "") }
        Assertions.assertEquals(expected(), actual)
    }

    private fun expected() =
        listOf(
            DraftTransaction(
                id = 1L,
                bankId = Bank.TINKOFF.id,
                date = operationDateTimeFormatter.parse("15.06.2024 12:34:56.000", LocalDateTime::from),
                sum = "-70.0",
                data = ""
            ),
            DraftTransaction(
                id = 2L,
                bankId = Bank.TINKOFF.id,
                date = operationDateTimeFormatter.parse("15.06.2024 12:34:56.001", LocalDateTime::from),
                sum = "-70.0",
                data = ""
            )
        )

    val operationDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss[.SSS]")

}