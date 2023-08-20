package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Transfer
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TransferRepository : JpaRepository<Transfer, Long>, BatchRepository<Transfer> {
    @Query("SELECT setval('transfer_id_seq', (SELECT MAX(id) FROM transfer))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE transfer CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class TransferRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Transfer>(
    namedParameterJdbcTemplate,
    "INSERT INTO transfer (id, name, transaction_id_from, transaction_id_to, available, validated) " +
            "VALUES (:id, :name, :transactionIdFrom, :transactionIdTo, :available, :validated)"
) {

}
