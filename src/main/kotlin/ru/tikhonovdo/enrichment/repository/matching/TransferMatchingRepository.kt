package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TransferMatchingRepository : JpaRepository<TransferMatching, Long>, BatchRepository<TransferMatching> {
    fun findByMatchingTransactionIdFrom(matchingTransactionIdFrom: Long): TransferMatching?
}

@Repository
class TransferMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<TransferMatching>(
    namedParameterJdbcTemplate,
    "INSERT INTO matching.transfer (name, matching_transaction_id_from, matching_transaction_id_to) " +
            "VALUES (:name, :matchingTransactionIdFrom, :matchingTransactionIdTo)"
)
