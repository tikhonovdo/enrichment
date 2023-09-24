package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TransactionMatchingRepository: JpaRepository<TransactionMatching, Long>, BatchRepository<TransactionMatching>, CustomTransactionMatchingRepository {
    fun existsByDraftTransactionId(draftTransactionId: Long): Boolean
}

interface CustomTransactionMatchingRepository {
    fun setEventIdForTransactions(eventId: Long?, matchingTransactionIds: Collection<Long>)
}

@Repository
class TransactionMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate): CustomTransactionMatchingRepository,
    AbstractBatchRepository<TransactionMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO matching.transaction (name, type, category_id, date, sum, account_id, description, event_id, validated, draft_transaction_id) " +
                "VALUES (:name, :typeId, :categoryId, :date, :sum, :accountId, :description, :eventId, :validated, :draftTransactionId)",
        "UPDATE matching.transaction SET " +
                "name = :name, type = :typeId, category_id = :categoryId, date = :date, sum = :sum, account_id = :accountId, " +
                "description = :description, event_id = :eventId, validated = :validated " +
                "WHERE draft_transaction_id = :draftTransactionId"
    ) {

    override fun setEventIdForTransactions(eventId: Long?, matchingTransactionIds: Collection<Long>) {
        namedParameterJdbcTemplate.batchUpdate(
            "UPDATE matching.transaction SET event_id = :eventId WHERE id IN (:matchingTransactionIds)",
            arrayOf(MapSqlParameterSource(mapOf("eventId" to eventId, "matchingTransactionIds" to matchingTransactionIds)))
        )
    }
}
