package ru.tikhonovdo.enrichment.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData
import ru.tikhonovdo.enrichment.repository.financepm.*

interface FinancePmService {
    fun saveData(contentAsByteArray: ByteArray)
    fun retrieveData(): ByteArray
}

@Service
class FinancePmServiceImpl(
    val currencyRepository: CurrencyRepository,
    val accountRepository: AccountRepository,
    val categoryRepository: CategoryRepository,
    val transactionRepository: TransactionRepository,
    val transferRepository: TransferRepository,
    val arrearRepository: ArrearRepository,
    val arrearTransactionRepository: ArrearTransactionRepository,
    val enrichmentJsonMapper: ObjectMapper
): FinancePmService {

    @Transactional
    override fun saveData(contentAsByteArray: ByteArray) {
        enrichmentJsonMapper.readValue(contentAsByteArray, FinancePmData::class.java).let { data ->
            resetFinancePmTables()
            save(data)
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun resetFinancePmTables() {
        arrearTransactionRepository.truncate()
        arrearRepository.truncate()
        transferRepository.truncate()
        transactionRepository.truncate()
        categoryRepository.truncate()
        accountRepository.truncate()
        currencyRepository.truncate()
    }

    private fun save(data: FinancePmData) {
        val currencies = data.currencies.sortedBy { it.id }
        val accounts = data.accounts.sortedBy { it.id }
        val categories = data.categories.sortedBy { it.id }
        val transactions = data.transactions.sortedBy { it.id }
        val transfers = data.transfers.sortedBy { it.id }.map { it.also { it.validated = true } }
        val arrears = data.arrears.sortedBy { it.id }
        val arrearTransaction = data.arrearTransaction.sortedBy { it.id }

        currencyRepository.insertBatchUnsafe(currencies)
        accountRepository.insertBatchUnsafe(accounts)
        categoryRepository.insertBatchUnsafe(categories)
        transactionRepository.insertBatchUnsafe(transactions)
        transferRepository.insertBatchUnsafe(transfers)
        arrearRepository.insertBatchUnsafe(arrears)
        arrearTransactionRepository.insertBatchUnsafe(arrearTransaction)

        currencyRepository.resetSequence()
        accountRepository.resetSequence()
        categoryRepository.resetSequence()
        transactionRepository.resetSequence()
        transferRepository.resetSequence()
        arrearRepository.resetSequence()
        arrearTransactionRepository.resetSequence()
    }

    override fun retrieveData(): ByteArray {
        val data = FinancePmData(
            version = 2,
            transactions = transactionRepository.findAll().toMutableList(),
            transfers = transferRepository.findAll().toMutableList(),
            accounts = accountRepository.findAll().toMutableList(),
            categories = categoryRepository.findAll().toMutableList(),
            currencies = currencyRepository.findAll().toMutableList(),
            arrears = arrearRepository.findAll().toMutableList(),
            arrearTransaction = arrearTransactionRepository.findAll().toMutableList(),
        )

        return enrichmentJsonMapper.writeValueAsBytes(data)
    }
}