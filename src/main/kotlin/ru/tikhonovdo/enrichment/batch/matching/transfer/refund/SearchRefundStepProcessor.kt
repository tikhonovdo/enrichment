package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class SearchRefundStepProcessor(private val transactionMatchingRepository: TransactionMatchingRepository): ItemProcessor<TransactionMatching, TransactionMatching> {

    override fun process(item: TransactionMatching): TransactionMatching? {
        val previousDay = item.date.toLocalDate().minusDays(1).atStartOfDay()
        val nextDay = item.date.toLocalDate().plusDays(1).atStartOfDay()
        val refundCandidates = transactionMatchingRepository.findAllByDateBetweenAndTypeIdEquals(
            previousDay, nextDay, Type.OUTCOME.id
        )
        val refundCandidate = refundCandidates
            .sortedByDescending { it.date }
            .firstOrNull { it.accountId == item.accountId && it.name.lowercase().contains(item.description.lowercase()) }

        return if (refundCandidate == null) {
            null
        } else {
            item.refundForId = refundCandidate.id
            item
        }
    }
}