package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import ru.tikhonovdo.enrichment.domain.Event
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.financepm.AccountRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository

class CashTransferMatchingStepWriter(
    private val accountRepository: AccountRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val transferMatchingRepository: TransferMatchingRepository
): ItemWriter<TransactionMatching> {

    override fun write(chunk: Chunk<out TransactionMatching>) {
        if (chunk.items.isEmpty()) {
            return
        }

        val transactionsFromTo = chunk.items.associateWith { TransactionMatching(
            draftTransactionId = null,
            name = it.name,
            typeId = Type.INCOME.id,
            categoryId = null,
            eventId = Event.TRANSFER.id,
            date = it.date,
            sum = it.sum,
            accountId = accountRepository.findCashAccountIdByAccountId(it.accountId!!)
        ) }
        transactionMatchingRepository.saveAll(transactionsFromTo.keys + transactionsFromTo.values)

        val transfers = transactionsFromTo.map { TransferMatching(
            name = it.key.name,
            matchingTransactionIdFrom = it.key.id!!,
            matchingTransactionIdTo = it.value.id!!,
            validated = true
        ) }
        transferMatchingRepository.saveAll(transfers)
    }
}