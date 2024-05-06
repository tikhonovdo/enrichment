package ru.tikhonovdo.enrichment.batch.matching.transfer.complement

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class TransferComplementStepProcessor(private val transactionMatchingRepository: TransactionMatchingRepository):
    ItemProcessor<TransferComplementInfo, Collection<TransactionMatching>> {

    override fun process(item: TransferComplementInfo): Collection<TransactionMatching> {
        val candidates = transactionMatchingRepository.findTransferCandidatesToCreateComplement(item)

        return candidates
            .map { it.copy() }
            .map {
                it.typeId = Type.swap(it.typeId)
                it.accountId = item.targetAccountId
                it
            }
    }
}