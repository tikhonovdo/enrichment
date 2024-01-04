package ru.tikhonovdo.enrichment.batch.matching.transaction.alfa

import ru.tikhonovdo.enrichment.batch.matching.transaction.AbstractTransactionStepProcessor
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.AlfaRecord
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class AlfaTransactionStepProcessor(
    draftTransactionRepository: DraftTransactionRepository,
    categoryMatchingRepository: CategoryMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
) : AbstractTransactionStepProcessor<AlfaRecord>(Bank.ALFA, draftTransactionRepository, categoryMatchingRepository) {

    override fun isInvalidTransaction(item: AlfaRecord): Boolean {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        return exists || item.status != "В обработке"
    }

    override fun getType(item: AlfaRecord): Type {
        return when (item.type.lowercase()) {
            "Пополнение" -> { Type.INCOME }
            "Списание" -> { Type.OUTCOME }
            else -> { throw IllegalStateException("Unknown type in AlfaRecord: ${item.type}") }
        }
    }

    override fun getAccountId(record: AlfaRecord): Long? =
        accountMatchingRepository.findByBankAccountCodeAndBankId(record.accountNumber, bank.id).accountId!!

}