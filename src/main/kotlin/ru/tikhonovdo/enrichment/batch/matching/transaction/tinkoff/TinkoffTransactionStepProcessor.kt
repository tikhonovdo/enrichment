package ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff

import ru.tikhonovdo.enrichment.batch.matching.transaction.AbstractTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TinkoffAccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

open class TinkoffTransactionStepProcessor(
    draftTransactionRepository: DraftTransactionRepository,
    categoryMatchingRepository: CategoryMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val tinkoffAccountMatchingRepository: TinkoffAccountMatchingRepository
) : AbstractTransactionStepProcessor<TinkoffRecord>(Bank.TINKOFF, draftTransactionRepository, categoryMatchingRepository) {

    override fun postProcess(item: TinkoffRecord, entity: TransactionMatching): TransactionMatching {
        item.brandName?.takeIf { it !in entity.name }?.let {
            entity.description = "${entity.description} $it".trim()
        }
        item.message?.let { entity.description = it }
        item.nomination?.let {
            entity.description += if (entity.description.isEmpty()) it else "; $it"
        }
        return entity
    }

    override fun isInvalidTransaction(item: TinkoffRecord): Boolean {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        return exists || item.status != "OK" || item.paymentDate == null
    }

    override fun getType(item: TinkoffRecord): Type {
        return item.type.toDomainType()
    }

    override fun getAccountId(record: TinkoffRecord): Long? =
        if (record.cardNumber == null && record.accountNumber == null) {
            tinkoffAccountMatchingRepository.findAccountId(record.paymentCurrency, record.description)
        } else {
            val bankAccountCode = record.accountNumber ?: record.cardNumber

            if (accountMatchingRepository.needToSkipAccount(bankAccountCode!!, bank.id)) {
                null
            } else {
                accountMatchingRepository.findByBankAccountCodeAndBankId(bankAccountCode, bank.id).accountId
            }
        }

}