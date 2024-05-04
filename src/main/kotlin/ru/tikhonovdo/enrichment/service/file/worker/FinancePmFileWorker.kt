package ru.tikhonovdo.enrichment.service.file.worker

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData
import ru.tikhonovdo.enrichment.repository.financepm.*
import ru.tikhonovdo.enrichment.service.file.worker.financepm.ImportMatchingData
import ru.tikhonovdo.enrichment.service.file.worker.financepm.ImportMatchingDataBuilder
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

interface FinancePmFileWorker : FileWorker {
    fun retrieveData(): ByteArray
}

@Component
class FinancePmFileWorkerImpl(
    val currencyRepository: CurrencyRepository,
    val accountRepository: AccountRepository,
    val categoryRepository: CategoryRepository,
    val transactionRepository: TransactionRepository,
    val transferRepository: TransferRepository,
    val arrearRepository: ArrearRepository,
    val arrearTransactionRepository: ArrearTransactionRepository,
    val importMatchingDataBuilder: ImportMatchingDataBuilder
) : FinancePmFileWorker {

    private val log = LoggerFactory.getLogger(FinancePmFileWorkerImpl::class.java)

    override fun saveData(saveMode: SaveMode, vararg content: ByteArray) {
        JSON_MAPPER.readValue(content[0], FinancePmData::class.java)?.let {
            save(it, saveMode)
        }
    }

    private fun save(data: FinancePmData, saveMode: SaveMode) {
        val currencies = data.currencies.sortedBy { it.id }
        val accounts = data.accounts.sortedBy { it.id }.map { it.also { it.balance = it.balance.setScale(6) } }
        val categories = data.categories.sortedBy { it.id }
        val transactions = data.transactions.sortedBy { it.id }.map { it.also { it.sum = it.sum.setScale(6) } }
        val transfers = data.transfers.sortedBy { it.id }
        val arrears = data.arrears.sortedBy { it.id }.map { it.also { it.balance = it.balance.setScale(6) } }
        val arrearTransactions = data.arrearTransaction.sortedBy { it.id }

        val importMatchingData: ImportMatchingData =
            if (saveMode == SaveMode.FULL_RESET) {
                log.info("Full reset requested")
                ImportMatchingData()
            } else {
                importMatchingDataBuilder.build(transactions, transfers)
            }

        saveData("currency", currencies, currencyRepository, saveMode)
        saveData("account", accounts, accountRepository, saveMode)
        saveData("category", categories, categoryRepository, saveMode)
        saveData("transaction", transactions, transactionRepository, saveMode) { importMatchingData.fix(it) }
        saveData("transfer", transfers, transferRepository, saveMode) { importMatchingData.fix(it) }
        saveData("arrear", arrears, arrearRepository, saveMode)
        saveData("arrearTransaction", arrearTransactions, arrearTransactionRepository, saveMode) { importMatchingData.fix(it) }
    }

    private fun <T : Any> saveData(
        type: String, entities: Collection<T>, repository: FinancePmRepository<T>,
        saveMode: SaveMode, importTransformFunction: (T) -> T? = { it }
    ) {
        var savedCount = 0
        if (entities.isEmpty()) {
            log.info("$type collection is empty. Skipping...")
            return
        }
        when (saveMode) {
            SaveMode.FULL_RESET -> {
                savedCount = repository.saveDataFromScratch(entities)
            }

            SaveMode.DEFAULT -> {
                val existedEntities = repository.findAll().toSet()
                entities.filter {
                    !existedEntities.contains(it)
                }.let { filtered ->
                    if (filtered.isNotEmpty()) {
                        val toSave = filtered.mapNotNull { it.let(importTransformFunction) }
                        savedCount = repository.insertBatch(toSave)
                        repository.updateSequence()
                    }
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