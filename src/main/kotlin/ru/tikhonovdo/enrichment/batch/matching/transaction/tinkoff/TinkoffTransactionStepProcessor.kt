package ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff

import ru.tikhonovdo.enrichment.batch.matching.transaction.AbstractTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.TinkoffRecord
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TinkoffAccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import kotlin.math.sign

class TinkoffTransactionStepProcessor(
    draftTransactionRepository: DraftTransactionRepository,
    categoryMatchingRepository: CategoryMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val tinkoffAccountMatchingRepository: TinkoffAccountMatchingRepository
) : AbstractTransactionStepProcessor<TinkoffRecord>(Bank.TINKOFF, draftTransactionRepository, categoryMatchingRepository) {

    override fun isInvalidTransaction(item: TinkoffRecord): Boolean {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        return exists || item.status != "OK" || item.paymentDate == null
    }

    override fun getType(item: TinkoffRecord): Type =
        if (item.paymentSum.sign > 0) {
            Type.INCOME
        } else {
            Type.OUTCOME
        }

    override fun getAccountId(record: TinkoffRecord): Long? =
        if (record.cardNumber == null) {
            tinkoffAccountMatchingRepository.findAccountId(record.paymentCurrency, record.description)
        } else {
            accountMatchingRepository.findByBankAccountCodeAndBankId(record.cardNumber, bank.id).accountId!!
        }

}