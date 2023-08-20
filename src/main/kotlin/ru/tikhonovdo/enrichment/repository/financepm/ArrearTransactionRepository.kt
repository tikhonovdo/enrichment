package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.ArrearTransaction
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface ArrearTransactionRepository : JpaRepository<ArrearTransaction, Long>, BatchRepository<ArrearTransaction> {
    @Query("SELECT setval('arrear_transaction_id_seq', (SELECT MAX(id) FROM arrear_transaction))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE arrear_transaction CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class ArrearTransactionRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<ArrearTransaction>(
    namedParameterJdbcTemplate,
    "INSERT INTO arrear_transaction (id, arrear_id, transaction_id) VALUES (:id, :arrearId, :transactionId)",
) {

}
