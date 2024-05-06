package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import ru.tikhonovdo.enrichment.domain.Event
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository

class TransferMatchingStepWriter(
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val transferMatchingRepository: TransferMatchingRepository
): ItemWriter<TransferMatching> {

    override fun write(chunk: Chunk<out TransferMatching>) {
        val items = chunk.items
        if (items.isNotEmpty()) {
            val transactionIds = items.map { it.getTransactionIds() }.flatten()
            transactionMatchingRepository.setEventIdForTransactions(Event.TRANSFER.id, transactionIds)
            transferMatchingRepository.insertBatch(items)
        }
    }
}