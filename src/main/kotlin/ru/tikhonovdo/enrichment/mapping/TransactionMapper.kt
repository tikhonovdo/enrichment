package ru.tikhonovdo.enrichment.mapping

import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.financepm.TransactionRecord
import ru.tikhonovdo.enrichment.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.util.initLastId
import java.time.LocalDateTime
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign

class TransactionMapper(
    private val categoryMapper: CategoryMapper,
    private val accountMapper: AccountMapper,
    private val financePmDataHolder: FinancePmDataHolder,
) {

    companion object {
        private const val EPS: Double = 0.005
    }

    private var _lastTransactionId: Int? = null
    private var lastTransactionId: Int
        get() {
            if (_lastTransactionId == null) {
                _lastTransactionId = initLastId(financePmDataHolder.data.transactions)
            }
            return _lastTransactionId ?: throw AssertionError("Set to null by another thread")
        }
        set(value) {
            _lastTransactionId = value
        }

    fun toTransferTransaction(record: TinkoffRecord) = toTransaction(record, true)

    fun toTransaction(record: TinkoffRecord, transfer: Boolean = false): TransactionRecord {
        return TransactionRecord(
            ++lastTransactionId,
            record.description,
            if (record.paymentSum.sign > 0) 1 else 2,
            if (transfer) 0 else categoryMapper.getFinancePmId(record),
            record.operationDate.toDate(),
            if (record.paymentSum.absoluteValue < EPS) 0.0 else record.paymentSum.absoluteValue,
            accountMapper.getFinancePmId(record),
            "",
            if (transfer) 1 else 0
        )
    }

    private fun LocalDateTime.toDate(): Date = java.sql.Timestamp.valueOf(this)
}