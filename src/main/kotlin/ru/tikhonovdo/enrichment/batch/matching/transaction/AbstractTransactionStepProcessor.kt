package ru.tikhonovdo.enrichment.batch.matching.transaction

import org.slf4j.LoggerFactory
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor
import org.springframework.lang.NonNull
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.BaseRecord
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.math.abs

abstract class AbstractTransactionStepProcessor<T: BaseRecord>(
    protected val bank: Bank,
    private val draftTransactionRepository: DraftTransactionRepository,
    private val categoryMatchingRepository: CategoryMatchingRepository,
) : ItemProcessor<T, TransactionMatching>, StepExecutionListener {

    private val log = LoggerFactory.getLogger(this::class.java)
    private var matchingCategories = listOf<CategoryMatching>()

    override fun beforeStep(stepExecution: StepExecution) {
        matchingCategories = categoryMatchingRepository.findAllByBankId(bank.id)
    }

    @Transactional
    override fun process(@NonNull item: T): TransactionMatching? {
        if (isInvalidTransaction(item)) {
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
        val resultEntity = TransactionMatching(
            draftTransactionId = item.draftTransactionId,
            name = item.description,
            typeId = getType(item).id,
            categoryId = categoryId,
            date = item.operationDate,
            sum = BigDecimal.valueOf(abs(item.paymentSum)),
            accountId = accountId,
            validated = !Stream.of(categoryId, accountId).anyMatch { it == null }
        )
        return postProcess(item, resultEntity)
    }

    protected open fun postProcess(item: T, entity: TransactionMatching): TransactionMatching {
        return entity
    }

    protected abstract fun isInvalidTransaction(item: T): Boolean

    protected abstract fun getType(item: T): Type

    protected abstract fun getAccountId(record: T): Long?

    private fun getCategoryId(record: T): Long? {
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
        countScore(draftCandidates.filter { it.matchingSumRule(record.paymentSum) })

        if (scoreMap.values.size != 1 && scoreMap.values.toSet().size == 1) {
            return null
        }

        val scores = scoreMap.entries.sortedByDescending { it.value }
        val finalCandidate = scores.first().key
        return if (
            (finalCandidate.mcc == null || finalCandidate.mcc == record.mcc)
            && (finalCandidate.pattern == null || record.description.contains(finalCandidate.pattern!!))
            && (finalCandidate.sum == null || finalCandidate.matchingSumRule(record.paymentSum))
        ) {
            finalCandidate.categoryId!!
        } else {
            null
        }
    }

    private fun CategoryMatching.matchingSumRule(recordSum: Double): Boolean {
        return if (sum == null) {
            false
        } else {
            val comparison = abs(recordSum).compareTo(sum!!)
            when (comparisonSign) {
                CategoryMatching.GTE -> comparison >= 0
                CategoryMatching.GT  -> comparison > 0
                CategoryMatching.LTE -> comparison <= 0
                CategoryMatching.LT  -> comparison < 0
                CategoryMatching.EQ  -> comparison == 0
                else -> false
            }
        }
    }

}