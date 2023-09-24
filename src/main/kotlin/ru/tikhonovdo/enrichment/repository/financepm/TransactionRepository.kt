package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import java.util.function.Function

interface TransactionRepository: JpaRepository<Transaction, Long>, FinancePmRepository<Transaction>

@Repository
class TransactionRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    AbstractFinancePmRepository<Transaction>(
        namedParameterJdbcTemplate,
        "financepm.transaction",
        Function { it.id },
        "INSERT INTO financepm.transaction (id, name, type, category_id, date, sum, account_id, description, event_id, available, matching_transaction_id) " +
                "VALUES (:id, :name, :typeId, :categoryId, :date, :sum, :accountId, :description, :eventId, :available, :matchingTransactionId)",
        "UPDATE financepm.transaction SET " +
                "name = :name, type = :typeId, category_id = :categoryId, date = :date, sum = :sum, account_id = :accountId, " +
                "description = :description, event_id = :eventId, available = :available, matching_transaction_id = :matchingTransactionId " +
                "WHERE id = :id"
    ) {

}
