package ru.tikhonovdo.enrichment.service.file.worker.financepm

import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.enitity.ArrearTransaction
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.domain.enitity.Transfer
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.financepm.TransferRepository

@Component
class ImportMatchingDataBuilder(
    private val transactionRepository: TransactionRepository,
    private val transferRepository: TransferRepository
) {

    fun build(transactions: List<Transaction>, transfers: List<Transfer>): ImportMatchingData {
        val importedToExistingIdMappingTransactions: MutableMap<Long, Long> = mutableMapOf()
        val importedToExistingIdMappingTransfers: MutableMap<Long, Long> = mutableMapOf()
        val transactionIdDiff: Long
        val transferIdDiff: Long

        val matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
            .withIgnorePaths("id", "matchingTransactionId")

        transactions.forEach {
            val existing = transactionRepository.findAll(Example.of(it, matcher), Pageable.unpaged())
            if (!existing.isEmpty) {
                importedToExistingIdMappingTransactions[it.id!!] = existing.first().id!!
            }
        }
        var exisingMaxId = importedToExistingIdMappingTransactions.values.max()
        var importingMaxId = importedToExistingIdMappingTransactions.keys.max()
        transactionIdDiff = if (exisingMaxId > importingMaxId) exisingMaxId - importingMaxId else 0

        transfers.forEach {
            val mappedTransfer = it.copy().also { transfer ->
                var existingId = importedToExistingIdMappingTransactions[transfer.transactionIdFrom]
                if (existingId != null) {
                    transfer.transactionIdFrom = existingId
                }
                existingId = importedToExistingIdMappingTransactions[transfer.transactionIdTo]
                if (existingId != null) {
                    transfer.transactionIdTo = existingId
                }
            }
            val existing = transferRepository.findAll(Example.of(mappedTransfer, matcher), Pageable.unpaged())
            if (!existing.isEmpty) {
                importedToExistingIdMappingTransfers[it.id!!] = existing.first().id!!
            }
        }
        exisingMaxId = importedToExistingIdMappingTransfers.values.max()
        importingMaxId = importedToExistingIdMappingTransfers.keys.max()
        transferIdDiff = if (exisingMaxId > importingMaxId) exisingMaxId - importingMaxId else 0

        return ImportMatchingData(
            importedToExistingIdMappingTransactions,
            importedToExistingIdMappingTransfers,
            transactionIdDiff,
            transferIdDiff
        )
    }
}

class ImportMatchingData(
    private val importedToExistingIdMappingTransactions: Map<Long, Long> = mapOf(),
    private val importedToExistingIdMappingTransfers: Map<Long, Long> = mapOf(),
    private val transactionIdDiff: Long = 0L,
    private val transferIdDiff: Long = 0L,
) {

    fun fix(entity: ArrearTransaction): ArrearTransaction? {
        val clone = entity.copy()
        return fixTransactionId(clone.transactionId) {
            clone.transactionId += transactionIdDiff
            clone
        }
    }

    fun fix(entity: Transaction): Transaction? {
        val clone = entity.copy()
        return fixTransactionId(clone.id!!) {
            clone.id = clone.id!! + transactionIdDiff
            clone
        }
    }

    fun fix(entity: Transfer): Transfer? {
        val transfer = entity.copy()

        val existingId = importedToExistingIdMappingTransfers[transfer.id]
        if (existingId == null) {
            transfer.id = transfer.id!! + transferIdDiff
            fixTransactionId(transfer.transactionIdFrom) { transfer.transactionIdFrom += transactionIdDiff }
            fixTransactionId(transfer.transactionIdTo) { transfer.transactionIdTo += transactionIdDiff }
        } else {
            return null
        }
        return transfer
    }

    private fun <T : Any> fixTransactionId(importedTransactionId: Long, idConsumer: (Long) -> (T)): T? {
        val existingId = importedToExistingIdMappingTransactions[importedTransactionId]
        return if (existingId == null) {
            idConsumer.invoke(importedTransactionId + transactionIdDiff)
        } else {
            null
        }
    }

}