package ru.tikhonovdo.enrichment.old.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.domain.enitity.Category
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.domain.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.old.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.repository.financepm.CategoryRepository
import ru.tikhonovdo.enrichment.service.MappingService
import java.text.SimpleDateFormat
import java.util.*

class TransactionProcessor(
    private val financePmDataHolder: FinancePmDataHolder,
    private val categoryRepository: CategoryRepository,
    private val mappingService: MappingService
) {

    fun performProcessing(rowsWithoutTransfers: List<TinkoffRecord>) {
        val additionalProcessingList = addTransactions(rowsWithoutTransfers)
        log.info("${additionalProcessingList.size} records left for additional processing")
        if (additionalProcessingList.isNotEmpty()) {
            performManualProcessing(additionalProcessingList)
        }
    }

    private fun addTransactions(transactionsToProcess: List<TinkoffRecord>): List<Transaction> {
        val forAdditionalProcessing = mutableListOf<Transaction>()
        val transactions = financePmDataHolder.data.transactions
        transactionsToProcess.forEach { record ->
            val transaction = mappingService.toTransaction(record)
            val duplicate = transactions.find { it == transaction }
            if (duplicate != null /*&& !duplicate.new*/) {
                processDuplicateTransaction(duplicate, transaction)
            } else {
                if (transaction.categoryId != null) {
                    transactions.add(transaction)
                } else {
                    forAdditionalProcessing.add(transaction)
                }
            }
        }
        return forAdditionalProcessing
    }

    private fun performManualProcessing(transactionRecords: List<Transaction>) {
        log.info("${transactionRecords.size} records left for manual processing")
        println("Categories:")
        val categories = financePmDataHolder.data.categories
        val transactions = financePmDataHolder.data.transactions

        categories.forEach {
            println("${it.id}\t${it.name}")
        }
        transactionRecords.forEach { it ->
            var categoryRecord: Category? = null

            println("Choose name for record (press enter for skip): $it ")
            val transactionName = readln()
            println("Choose category for record above")
            while (categoryRecord == null) {
                categoryRecord = categoryRepository.findByName(readln())
                if (categoryRecord == null) println("Wrong category name. Please, try again.")
            }
            it.categoryId = categoryRecord.id
            println("CategoryId ${categoryRecord.id} set")
            transactions.add(it)

            it.name = transactionName.ifBlank { it.name }
        }
    }

    fun processDuplicateTransaction(duplicate: Transaction, transaction: Transaction) {
        if (defaultTransactionNames.contains(duplicate.name)) {
            log.info("Duplicate found on ${duplicate.date}: '${duplicate.name}' replaced with '${transaction.name}'.")
            duplicate.name = transaction.name
        } else {
            log.info("Duplicate found on ${duplicate.date}: '${duplicate.name}'. Nothing was changed.")
        }
    }

    private val sdt: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    private fun Date.formatted(): String = sdt.format(this)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TransactionProcessor::class.java)
        private val defaultTransactionNames = setOf("Доход", "Расход")
    }
}