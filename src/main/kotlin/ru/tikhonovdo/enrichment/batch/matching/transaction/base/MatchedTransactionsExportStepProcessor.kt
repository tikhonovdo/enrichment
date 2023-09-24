package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import org.springframework.batch.item.ItemProcessor
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository

class MatchedTransactionsExportStepProcessor(private val transactionRepository: TransactionRepository) : ItemProcessor<TransactionMatching, Transaction> {
    override fun process(transactionMatching: TransactionMatching): Transaction? {
        val transaction = transactionMatching.toTransaction()
        val matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
            .withIgnorePaths("name", "description", "available", "matchingTransactionId")
            .withMatcher("typeId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("categoryId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("accountId", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("sum", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("eventId", ExampleMatcher.GenericPropertyMatchers.exact())
        val query = transactionRepository.findAll(Example.of(transaction, matcher))

        return if (query.isNotEmpty()) {
            query.firstOrNull()?.apply {
                matchingTransactionId = transaction.matchingTransactionId
            }
        } else {
            transaction
        }
    }

    private fun TransactionMatching.toTransaction(): Transaction = Transaction(
        name = name,
        typeId = typeId,
        categoryId = categoryId,
        date = date,
        sum = sum,
        accountId = accountId,
        description = description,
        eventId = eventId,
        matchingTransactionId = id
    )

}