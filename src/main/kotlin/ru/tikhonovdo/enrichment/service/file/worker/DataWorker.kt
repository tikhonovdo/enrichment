package ru.tikhonovdo.enrichment.service.file.worker

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.DataType
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository

interface DataWorker {
    fun getDataType(): DataType
}

interface FinancePmDataWorker : DataWorker {

    fun saveData(saveMode: SaveMode, vararg content: ByteArray)

    fun retrieveData(): ByteArray

    override fun getDataType() = DataType.FINANCE_PM
}

abstract class BankDataWorker(
    private val draftTransactionRepository: DraftTransactionRepository,
    private val bank: Bank
): DataWorker {

    protected val log = LoggerFactory.getLogger(BankDataWorker::class.java)

    override fun getDataType() =  DataType.fromBank(bank)

    protected abstract fun toDraftTransactionList(json: String): List<DraftTransaction>

    @Transactional
    open fun saveDrafts(json: String) {
        val drafts = toDraftTransactionList(json)

        val minDate = drafts.minBy { it.date }.date
        val maxDate = drafts.maxBy { it.date }.date

        val deleted = draftTransactionRepository.deleteObsoleteDraft(bank)
        log.info("$deleted drafts are obsolete and has been deleted")

        val existingDraftsIds = draftTransactionRepository.findAllByBankIdAndDateBetween(bank.id, minDate, maxDate)
            .map { it.innerBankId }
            .toSet()
        drafts.filter {
            !existingDraftsIds.contains(it.innerBankId)
        }.let {
            var inserted = 0
            if (it.isNotEmpty()) {
                inserted = draftTransactionRepository.insertBatch(it)
            }
            log.info("Upload success. $inserted records was inserted")
        }
    }

}