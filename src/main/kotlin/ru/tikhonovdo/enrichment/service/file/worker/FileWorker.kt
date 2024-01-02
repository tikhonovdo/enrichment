package ru.tikhonovdo.enrichment.service.file.worker

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository

interface FileWorker {
    fun saveData(content: ByteArray, fullReset: Boolean = false)
}

abstract class BankFileWorker(
    private val draftTransactionRepository: DraftTransactionRepository,
    private val bank: Bank
): FileWorker {

    private val log = LoggerFactory.getLogger(BankFileWorker::class.java)

    @Transactional
    override fun saveData(content: ByteArray, fullReset: Boolean) {
        saveData(content)
    }

    fun saveData(content: ByteArray) {
        val deleted = draftTransactionRepository.deleteObsoleteDraft()
        log.info("$deleted drafts are obsolete and has been deleted")

        val drafts = readFile(content)
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
            log.info("Upload success. $inserted records for was inserted")
        }
    }

    protected abstract fun readFile(content: ByteArray): List<DraftTransaction>

}