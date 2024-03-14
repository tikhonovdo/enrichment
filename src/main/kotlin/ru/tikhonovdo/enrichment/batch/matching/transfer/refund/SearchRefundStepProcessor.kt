package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class SearchRefundStepProcessor(private val transactionMatchingRepository: TransactionMatchingRepository): ItemProcessor<TransactionMatching, TransactionMatching> {

    override fun process(item: TransactionMatching): TransactionMatching? {
        val refundCandidates = transactionMatchingRepository.findAllByDateBetweenAndTypeIdEquals(
            item.date.minusDays(1), item.date, Type.OUTCOME.id
        )
        val refundCandidate = refundCandidates
            .sortedByDescending { it.date }
            .firstOrNull { it.accountId == item.accountId && it.name == item.description }

        return if (refundCandidate == null) {
            null
        } else {
            item.refundForId = refundCandidate.id
            item
        }
    }
}