package ru.tikhonovdo.enrichment.service.file.worker

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData
import ru.tikhonovdo.enrichment.repository.financepm.*
import ru.tikhonovdo.enrichment.service.file.FileServiceWorker
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

interface FinancePmFileWorker: FileServiceWorker {
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
    override fun saveData(content: ByteArray, fullReset: Boolean) {
        JSON_MAPPER.readValue(content, FinancePmData::class.java)?.let {
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

        if (fullReset) {
            log.info("Full reset requested")
        }
        saveData("currency", currencies, currencyRepository, fullReset)
        saveData("account", accounts, accountRepository, fullReset)
        saveData("category", categories, categoryRepository, fullReset)
        saveData("transaction", transactions, transactionRepository, fullReset) { it.also { it.matchingTransactionId = null } }
        saveData("transfer", transfers, transferRepository, fullReset)
        saveData("arrear", arrears, arrearRepository, fullReset)
        saveData("arrearTransaction", arrearTransaction, arrearTransactionRepository, fullReset)
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