package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import java.time.LocalDateTime

interface DraftTransactionRepository : JpaRepository<DraftTransaction, Long>, CustomDraftTransactionRepository

interface CustomDraftTransactionRepository {
    fun save(draftTransaction: DraftTransaction)
    fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching>
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

    override fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching> {
        return namedParameterJdbcTemplate.query("""
            SELECT DISTINCT ON (items.item->>'category', items.item->>'mcc')
                items.item->>'category' as bankCategoryName,
                items.item->>'mcc' as mcc,
                items.item->>'description' as pattern
            FROM
                 (SELECT jsonb_array_elements(data) item
                  FROM draft_transaction
                  WHERE bank_id = 1) items;
            """.trimIndent(),
            MapSqlParameterSource(mapOf("bankId" to bank.id))
        ) { rs, _ ->
                CategoryMatching(
                    bankId = bank.id,
                    bankCategoryName = rs.getString("bankCategoryName"),
                    mcc = rs.getInt("mcc").toString(),
                    pattern = rs.getString("pattern"),
                    categoryId = null
                )
        }
    }
}
