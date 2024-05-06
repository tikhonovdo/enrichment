package ru.tikhonovdo.enrichment.service.file.worker

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository

interface FileWorker {
    fun saveData(saveMode: SaveMode = SaveMode.DEFAULT, vararg content: ByteArray)
}

interface FinancePmFileWorker : FileWorker {
    fun retrieveData(): ByteArray
}

abstract class BankFileWorker(
    private val draftTransactionRepository: DraftTransactionRepository,
    private val bank: Bank
): FileWorker {

    private val log = LoggerFactory.getLogger(BankFileWorker::class.java)

    @Transactional
    override fun saveData(saveMode: SaveMode, vararg content: ByteArray) {
        saveData(*content)
    }

    fun saveData(vararg content: ByteArray) {
        val deleted = draftTransactionRepository.deleteObsoleteDraft()
        log.info("$deleted drafts are obsolete and has been deleted")

        val drafts = readBytes(*content)
        val minDate = drafts.minBy { it.date }.date
        val maxDate = drafts.maxBy { it.date }.date
        val existingDrafts = draftTransactionRepository.findAllByBankIdAndDateBetween(bank.id, minDate, maxDate)

        drafts.filter {
            !existingDrafts.contains(it)
        }.let {
            var inserted = 0
            if (it.isNotEmpty()) {
                inserted = draftTransactionRepository.insertBatch(it)
            }
            log.info("Upload success. $inserted records was inserted")
        }
    }

    protected abstract fun readBytes(vararg content: ByteArray): List<DraftTransaction>

}