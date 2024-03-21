package ru.tikhonovdo.enrichment.batch.matching.transfer.pattern

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class TransferPatternPreMatchingStepProcessor(
    private val transactionMatchingRepository: TransactionMatchingRepository
): ItemProcessor<PatternTransferMatchingInfo, List<TransactionMatching>> {

    override fun process(transferInfo: PatternTransferMatchingInfo): List<TransactionMatching> {
        val candidates = transactionMatchingRepository.findTransferCandidatesToComplement(
            transferInfo.sourceName,
            transferInfo.sourceDescription,
            transferInfo.sourceType,
            transferInfo.sourceAccountId
        )

        return candidates
            .map { it.copy() }
            .map {
                it.typeId = Type.swap(it.typeId)
                it.accountId = transferInfo.targetAccountId
                it
            }
    }

}