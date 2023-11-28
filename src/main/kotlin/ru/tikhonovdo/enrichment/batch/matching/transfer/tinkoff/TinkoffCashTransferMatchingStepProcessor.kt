package ru.tikhonovdo.enrichment.batch.matching.transfer.tinkoff

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class TinkoffCashTransferMatchingStepProcessor(private val transactionMatchingRepository: TransactionMatchingRepository):
    ItemProcessor<TransactionMatching, TransactionMatching> {

    override fun process(item: TransactionMatching): TransactionMatching? {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        return if (!exists) {
            item
        } else {
            null
        }
    }
}