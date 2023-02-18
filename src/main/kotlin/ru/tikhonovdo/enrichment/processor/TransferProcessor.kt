package ru.tikhonovdo.enrichment.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.financepm.TransferRecord
import ru.tikhonovdo.enrichment.financepm.getNextId

import ru.tikhonovdo.enrichment.mapping.TransactionMapper
import ru.tikhonovdo.enrichment.tinkoff.TinkoffRecord
import java.time.format.DateTimeFormatter
import kotlin.math.sign

class TransferProcessor(
    private val financePmDataHolder: FinancePmDataHolder,
    private val transactionMapper: TransactionMapper,
    private val transactionProcessor: TransactionProcessor
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TransferProcessor::class.java)
    }

    private val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val transactions
        get() = financePmDataHolder.data.transactions
    private val transfers
        get() = financePmDataHolder.data.transfers

    private var _lastTransferId: Int? = null
    private var lastTransferId: Int
        get() {
            if (_lastTransferId == null) {
                _lastTransferId = financePmDataHolder.data.transfers.getNextId()
            }
            return _lastTransferId ?: throw AssertionError("Set to null by another thread")
        }
        set(value) {
            _lastTransferId = value
        }

    fun addTransfers(transfers: List<Pair<TinkoffRecord, TinkoffRecord>>) {
        if (transfers.isEmpty()) return

        transfers.forEach { it ->
            val first = transactionMapper.toTransferTransaction(it.first)
            val second = transactionMapper.toTransferTransaction(it.second)

            // Здесь возможны коллизии. Например, при оплате в одном дне стандартных покупок
            // типа проезда в общественном транспорте. Нужно предусмотреть механизм распознавания таких ситуаций
            val firstDuplicate = transactions.find { it == first && it.categoryId == 0 }
            val secondDuplicate = transactions.find { it == second && it.categoryId == 0 }
            if (firstDuplicate != null && secondDuplicate != null) {
                log.info("Duplicate transfer found:")
                transactionProcessor.processDuplicateTransaction(firstDuplicate, first)
                transactionProcessor.processDuplicateTransaction(secondDuplicate, second)
            } else {
                transactions.add(first)
                transactions.add(second)
                this.transfers.add(
                    TransferRecord(++lastTransferId, first.name, first.id, second.id)
                )
            }
        }
    }

    fun findTransfers(rows: List<TinkoffRecord>): List<Pair<TinkoffRecord, TinkoffRecord>> {
        val transfers = mutableListOf<Pair<TinkoffRecord, TinkoffRecord>>()

        transfers.addAll(rows.filter { it.category == "Наличные" }
            .map { it to it.toCashTransfer() })

        transfers.addAll(rows.filter { it.description == "Перевод между счетами" }
            .groupBy { it.operationDate.format(shortDateTimeFormatter) }
            .filter { it.value.size == 2 }
            .map {
                val first = it.value[0]
                val second = it.value[1]
                if (first.paymentSum.sign < 0) {
                    first to second
                } else {
                    second to first
                }
            })

        return transfers
    }
}