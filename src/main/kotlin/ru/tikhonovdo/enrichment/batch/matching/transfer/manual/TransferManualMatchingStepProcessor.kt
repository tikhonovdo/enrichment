package ru.tikhonovdo.enrichment.batch.matching.transfer.manual

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Event
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository

class TransferManualMatchingStepProcessor(
    private val transferMatchingRepository: TransferMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository
): ItemProcessor<TransferManualMatchingInfo, Collection<TransferMatching>> {

    override fun process(transferInfo: TransferManualMatchingInfo): Collection<TransferMatching> {
        val result = transferMatchingRepository.findTransfersToManualMatch(transferInfo)

        transactionMatchingRepository.setEventIdForTransactions(
            Event.TRANSFER.id,
            result.map { listOf(it.matchingTransactionIdFrom, it.matchingTransactionIdTo) }.flatten()
        )

        return result
    }

}