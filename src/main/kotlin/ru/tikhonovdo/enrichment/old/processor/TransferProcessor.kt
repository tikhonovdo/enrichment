package ru.tikhonovdo.enrichment.old.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.domain.enitity.Transfer
import ru.tikhonovdo.enrichment.domain.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord
import ru.tikhonovdo.enrichment.service.MappingService
import java.time.format.DateTimeFormatter
import kotlin.math.sign

class TransferProcessor(
    private val financePmDataHolder: FinancePmDataHolder,
    private val transactionProcessor: TransactionProcessor,
    private val mappingService: MappingService
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TransferProcessor::class.java)
    }

    private val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val transactions
        get() = financePmDataHolder.data.transactions
    private val transfers
        get() = financePmDataHolder.data.transfers

    fun addTransfers(transfers: List<Pair<TinkoffRecord, TinkoffRecord>>) {
        if (transfers.isEmpty()) return

        transfers.forEach { it ->
            val first = mappingService.toTransferTransaction(it.first)
            val second = mappingService.toTransferTransaction(it.second)

            // Здесь возможны коллизии. Например, при оплате в одном дне стандартных покупок
            // типа проезда в общественном транспорте. Нужно предусмотреть механизм распознавания таких ситуаций
            val firstDuplicate = transactions.find { it == first && it.categoryId == 0L }
            val secondDuplicate = transactions.find { it == second && it.categoryId == 0L }
            if (firstDuplicate != null && secondDuplicate != null) {
                log.info("Duplicate transfer found:")
                transactionProcessor.processDuplicateTransaction(firstDuplicate, first)
                transactionProcessor.processDuplicateTransaction(secondDuplicate, second)
            } else {
                transactions.add(first)
                transactions.add(second)
                this.transfers.add(
                    Transfer(name = first.name,
                        transactionIdFrom = first.id!!,
                        transactionIdTo = second.id!!)
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