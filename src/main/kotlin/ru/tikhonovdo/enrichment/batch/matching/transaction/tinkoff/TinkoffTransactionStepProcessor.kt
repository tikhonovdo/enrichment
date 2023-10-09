package ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff

import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor
import org.springframework.lang.NonNull
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TinkoffAccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.sign

class TinkoffTransactionStepProcessor(
    private val draftTransactionRepository: DraftTransactionRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val tinkoffAccountMatchingRepository: TinkoffAccountMatchingRepository
) : ItemProcessor<TinkoffRecord, TransactionMatching>, StepExecutionListener {

    private val log = LoggerFactory.getLogger(TinkoffTransactionStepProcessor::class.java)

    private val bank = Bank.TINKOFF
    private var matchingCategories = listOf<CategoryMatching>()

    override fun beforeStep(stepExecution: StepExecution) {
        matchingCategories = categoryMatchingRepository.findAllByBankId(bank.id)
    }

    override fun process(@NonNull item: TinkoffRecord): TransactionMatching? {
        val exists = transactionMatchingRepository.existsByDraftTransactionId(item.draftTransactionId!!)
        if (exists || item.status != "OK" || item.paymentDate == null) {
            return null
        }

        draftTransactionRepository.findAllByBankIdAndDate(bank.id, item.operationDate).let {
            if (it.size > 1) {
                log.info(
                    "Found more than one record for selected Bank $bank on date: $it." +
                            " Please, check it manually in table financepm.transaction"
                )
            }
        }

        val categoryId = getCategoryId(item)
        val accountId = getAccountId(item)
        return TransactionMatching(
            draftTransactionId = item.draftTransactionId,
            name = item.description,
            typeId = getType(item).id,
            categoryId = categoryId,
            date = item.operationDate,
            sum = BigDecimal.valueOf(abs(item.paymentSum)),
            accountId = accountId,
            validated = !Stream.of(categoryId, accountId).anyMatch { it == null }
        )
    }

    private fun getType(item: TinkoffRecord): Type {
        return if (item.paymentSum.sign > 0) {
            Type.INCOME
        } else {
            Type.OUTCOME
        }
    }

    private fun getAccountId(record: TinkoffRecord): Long? =
        if (record.cardNumber == null) {
            tinkoffAccountMatchingRepository.findAccountId(record.paymentCurrency, record.description)
        } else {
            accountMatchingRepository.findByBankAccountCodeAndBankId(record.cardNumber, bank.id).accountId!!
        }

    private fun getCategoryId(record: TinkoffRecord): Long? {
        val typeId = getType(record).id
        val draftCandidates = matchingCategories
            .filter { it.category!!.typeId == typeId }
            .filter { it.bankCategoryName == record.category }

        if (draftCandidates.isEmpty()) {
            return null
        }

        val scoreMap = draftCandidates.associateWith { 0 }.toMutableMap()
        fun countScore(candidates: List<CategoryMatching>) {
            candidates.forEach { candidate ->
                if (scoreMap[candidate] != null) {
                    scoreMap[candidate] = scoreMap[candidate]!!.inc()
                }
            }
        }

        countScore(draftCandidates.filter { it.mcc == record.mcc })
        countScore(draftCandidates.filter { it.pattern != null && record.description.contains(it.pattern!!) })
        countScore(draftCandidates.filter { it.sum == abs(record.paymentSum) })

        if (scoreMap.values.size != 1 && scoreMap.values.toSet().size == 1) {
            return null
        }

        val scores = scoreMap.entries.sortedByDescending { it.value }
        val finalCandidate = scores.first().key
        return if (
            (finalCandidate.mcc == null || finalCandidate.mcc == record.mcc)
            && (finalCandidate.pattern == null || record.description.contains(finalCandidate.pattern!!))
            && (finalCandidate.sum == null || finalCandidate.sum == abs(record.paymentSum))
        ) {
            finalCandidate.categoryId!!
        } else {
            null
        }
    }

}