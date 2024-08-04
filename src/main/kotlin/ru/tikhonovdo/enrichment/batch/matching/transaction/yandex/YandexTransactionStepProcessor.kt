package ru.tikhonovdo.enrichment.batch.matching.transaction.yandex

import ru.tikhonovdo.enrichment.batch.matching.transaction.AbstractTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.Direction
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YandexRecord
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

open class YandexTransactionStepProcessor(
    draftTransactionRepository: DraftTransactionRepository,
    categoryMatchingRepository: CategoryMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
) : AbstractTransactionStepProcessor<YandexRecord>(Bank.YANDEX, draftTransactionRepository, categoryMatchingRepository) {

    override fun isInvalidTransaction(item: YandexRecord): Boolean {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        return exists || item.status != "CLEAR"
    }

    override fun postProcess(item: YandexRecord, entity: TransactionMatching): TransactionMatching {
        if (item.comment != null) {
            entity.description = item.comment
        }
        return entity
    }

    override fun getType(item: YandexRecord): Type {
        return when (item.direction) {
            Direction.CREDIT -> Type.INCOME
            Direction.DEBIT -> Type.OUTCOME
        }
    }

    override fun getAccountId(record: YandexRecord): Long? =
        accountMatchingRepository.findByBankAccountCodeAndBankId(record.accountName, bank.id).accountId!!

}