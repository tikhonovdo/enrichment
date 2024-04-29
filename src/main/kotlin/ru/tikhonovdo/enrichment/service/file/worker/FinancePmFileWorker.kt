package ru.tikhonovdo.enrichment.service.file.worker

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData
import ru.tikhonovdo.enrichment.repository.financepm.*
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

interface FinancePmFileWorker: FileWorker {
    fun retrieveData(): ByteArray
}

@Component
class FinancePmFileWorkerImpl (
    val currencyRepository: CurrencyRepository,
    val accountRepository: AccountRepository,
    val categoryRepository: CategoryRepository,
    val transactionRepository: TransactionRepository,
    val transferRepository: TransferRepository,
    val arrearRepository: ArrearRepository,
    val arrearTransactionRepository: ArrearTransactionRepository,
    val entityManager: EntityManager
): FinancePmFileWorker {

    private val log = LoggerFactory.getLogger(FinancePmFileWorkerImpl::class.java)

    @Transactional
    override fun saveData(fullReset: Boolean, vararg content: ByteArray) {
        JSON_MAPPER.readValue(content[0], FinancePmData::class.java)?.let {
            save(it, fullReset)
        }
    }

    private fun save(data: FinancePmData, fullReset: Boolean) {
        val currencies = data.currencies.sortedBy { it.id }
        val accounts = data.accounts.sortedBy { it.id }.map { it.also { it.balance = it.balance.setScale(6) } }
        val categories = data.categories.sortedBy { it.id }
        val transactions = data.transactions.sortedBy { it.id }.map { it.also { it.sum = it.sum.setScale(6) } }
        val transfers = data.transfers.sortedBy { it.id }
        val arrears = data.arrears.sortedBy { it.id }.map { it.also { it.balance = it.balance.setScale(6) } }
        val arrearTransaction = data.arrearTransaction.sortedBy { it.id }

        when (saveMode) {
            SaveMode.FULL_RESET -> {
                log.info("Full reset requested")
            }
            SaveMode.APPEND -> {
                var transactionDiff = 0L
                transactionRepository.getLastId().ifPresent { lastTransactionId ->
                    val minTransactionId = transactions.minOf { it.id!! }
                    transactionDiff = lastTransactionId - minTransactionId + 1
                    transactions.forEach { it.id = it.id!! + transactionDiff }
                    arrearTransaction.forEach {
                        it.transactionId += transactionDiff
                    }
                }
                transferRepository.getLastId().ifPresent { lastTransferId ->
                    val minTransferId = transfers.minOf { it.id!! }
                    val transferDiff = lastTransferId - minTransferId + 1
                    transfers.forEach {
                        it.id = it.id!! + transferDiff
                        it.transactionIdFrom += transactionDiff
                        it.transactionIdTo += transactionDiff
                    }
                }
            }

            else -> {}
        }

        saveData("currency", currencies, currencyRepository, saveMode)
        saveData("account", accounts, accountRepository, saveMode)
        saveData("category", categories, categoryRepository, saveMode)
        saveData("transaction", transactions, transactionRepository, saveMode) { it.also { it.matchingTransactionId = null } }
        saveData("transfer", transfers, transferRepository, saveMode)
        saveData("arrear", arrears, arrearRepository, saveMode)
        saveData("arrearTransaction", arrearTransaction, arrearTransactionRepository, saveMode)
    }

    private fun <T: Any> saveData(type: String, entities: Collection<T>, repository: FinancePmRepository<T>, fullReset: Boolean,
                                  clearingFunction: java.util.function.Function<T, T> = java.util.function.Function.identity()) {
        var savedCount = 0
        if (fullReset) {
            savedCount = repository.saveDataFromScratch(entities)
        } else {
            val existedEntities = repository.findAll().map {
                entityManager.detach(it)
                clearingFunction.apply(it)
            }
            entities.filter {
                !existedEntities.contains(it)
            }.let {
                if (it.isNotEmpty()) {
                    savedCount = repository.insertBatch(it)
                    repository.updateSequence()
                }
            }
        }

        if (savedCount > 0) {
            log.info("$savedCount $type records has been saved")
        } else {
            log.info("There is no new $type records")
        }
    }

    override fun retrieveData(): ByteArray {
        val data = FinancePmData(
            version = 2,
            transactions = transactionRepository.findAll(),
            transfers = transferRepository.findAll(),
            accounts = accountRepository.findAll(),
            categories = categoryRepository.findAll(),
            currencies = currencyRepository.findAll(),
            arrears = arrearRepository.findAll(),
            arrearTransaction = arrearTransactionRepository.findAll(),
        )

        return JSON_MAPPER.writeValueAsBytes(data)
    }
}