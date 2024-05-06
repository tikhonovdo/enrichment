package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository

class TransferMatchingStepProcessor(private val transferMatchingRepository: TransferMatchingRepository):
    ItemProcessor<TransferMatching, TransferMatching> {

    override fun process(item: TransferMatching): TransferMatching? {
        val result = transferMatchingRepository.findByMatchingTransactionIdFrom(item.matchingTransactionIdFrom)
        return if (result == null) {
            item
        } else {
            null
        }
    }
}