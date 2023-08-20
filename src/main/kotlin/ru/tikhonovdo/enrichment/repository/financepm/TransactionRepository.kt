package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Transaction
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TransactionRepository : JpaRepository<Transaction, Long>, BatchRepository<Transaction> {
    @Query("SELECT setval('transaction_id_seq', (SELECT MAX(id) FROM transaction))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE transaction CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class TransactionRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Transaction>(
    namedParameterJdbcTemplate,
    "INSERT INTO transaction (id, name, type, category_id, date, sum, account_id, description, event_id, available, bank_id) " +
            "VALUES (:id, :name, :typeId, :categoryId, :date, :sum, :accountId, :description, :eventId, :available, :bankId)"
    ) {

}
