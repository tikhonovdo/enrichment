package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class ApplyRefundStepProcessor(
    private val refundIncomeCategoryId: Long,
    private val transactionRepository: TransactionRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
): ItemProcessor<ApplyRefundInfo, Transaction> {

    override fun process(refundInfo: ApplyRefundInfo): Transaction? {
        val transactionToRefund = transactionRepository.findByMatchingTransactionId(refundInfo.refundForId)?.copy() ?: return null

        val startSign = transactionToRefund.sum.signum()
        transactionToRefund.sum = transactionToRefund.sum.minus(refundInfo.sum)
        val finishSign = transactionToRefund.sum.signum()

        if (startSign != finishSign) {
            transactionToRefund.sum = transactionToRefund.sum.abs()
            transactionToRefund.typeId = Type.swap(transactionToRefund.typeId)
            transactionToRefund.categoryId = refundIncomeCategoryId
        }

        transactionMatchingRepository.markValidated(refundInfo.sourceId)

        return transactionToRefund
    }
}