package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import jakarta.transaction.Transactional
import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

open class ApplyRefundStepProcessor(
    private val refundIncomeCategoryId: Long,
    private val transactionRepository: TransactionRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository
): ItemProcessor<ApplyRefundInfo, ApplyRefundInfo?> {

    @Transactional
    override fun process(refundInfo: ApplyRefundInfo): ApplyRefundInfo? {
        val transactionToRefund = transactionRepository.findByMatchingTransactionId(refundInfo.refundForId) ?: return null

        val startSign = transactionToRefund.sum.signum()
        transactionToRefund.sum = transactionToRefund.sum.minus(refundInfo.sum)
        val finishSign = transactionToRefund.sum.signum()

        if (startSign != finishSign) {
            transactionToRefund.sum = transactionToRefund.sum.abs()
            transactionToRefund.typeId = Type.swap(transactionToRefund.typeId)
            transactionToRefund.categoryId = refundIncomeCategoryId
        }
        transactionRepository.flush()
        transactionMatchingRepository.markValidated(listOf(refundInfo.sourceId, refundInfo.refundForId))

        return refundInfo
    }
}