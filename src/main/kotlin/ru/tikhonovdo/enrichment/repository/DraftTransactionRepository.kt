package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import java.time.LocalDateTime

interface DraftTransactionRepository : JpaRepository<DraftTransaction, Long>, CustomDraftTransactionRepository

interface CustomDraftTransactionRepository {
    fun save(draftTransaction: DraftTransaction)
}

@Repository
class DraftTransactionRepositoryImpl(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
): CustomDraftTransactionRepository {
    override fun save(draftTransaction: DraftTransaction) {
        namedParameterJdbcTemplate.update(
            "INSERT INTO draft_transaction (bank_id, upload_date, data) VALUES (:bankId, :uploadDate, :data::json)",
            MapSqlParameterSource(mapOf(
                "bankId" to draftTransaction.bankId,
                "uploadDate" to LocalDateTime.now(),
                "data" to draftTransaction.data
            ))
        )
    }
}
