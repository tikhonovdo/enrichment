package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository
import java.time.LocalDateTime
import java.util.*

interface TransactionMatchingRepository: JpaRepository<TransactionMatching, Long>,
    BatchRepository<TransactionMatching>, CustomTransactionMatchingRepository {
    @Query("""
        SELECT mt.date FROM matching.transaction mt
        JOIN matching.draft_transaction mdt on mt.draft_transaction_id = mdt.id
        left join financepm.category fc on mt.category_id = fc.id
        left join financepm.type tr_t ON mt.type = tr_t.id
        left join financepm.type cat_t ON fc.type = cat_t.id
        WHERE mdt.bank_id = :bankId AND (NOT (
                (account_id IS NULL) -- не задан счет
                OR ((category_id IS NOT NULL AND event_id IS NOT NULL) -- обозначено как событие и задана категория
                OR (category_id IS NULL AND event_id IS NULL)) -- не является событием и не задана категория
                OR (tr_t.id != cat_t.id) -- тип категории не соответвует типу транзацкии)
            ) OR validated)
        ORDER BY mt.date DESC LIMIT 1
    """, nativeQuery = true)
    fun findLastValidatedTransactionDateByBank(bankId: Long): Optional<LocalDateTime>

    fun existsByDraftTransactionId(draftTransactionId: Long): Boolean
}

interface CustomTransactionMatchingRepository {
    fun setEventIdForTransactions(eventId: Long?, matchingTransactionIds: Collection<Long>)

    fun getUnmatchedTransactionIds(): List<Long>

    fun deleteByIdIn(ids: Collection<Long>): Int

    fun updateSequence()
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

    override fun getUnmatchedTransactionIds(): List<Long> {
        return jdbcTemplate.queryForList(
        """
            SELECT mt.id
                -- mt.name, tr_t.name as transaction_type, fc.name as category, mt.category_id, cat_t.name as category_type, mt.date, mt.sum, mt.account_id, fa.name, mt.description, mt.event_id, mt.draft_transaction_id, mt.validated
            FROM matching.transaction mt
                     left join financepm.category fc on mt.category_id = fc.id
                     left join financepm.account fa on mt.account_id = fa.id
                     left join financepm.type tr_t ON mt.type = tr_t.id
                     left join financepm.type cat_t ON fc.type = cat_t.id
            WHERE (
                (account_id IS NULL) -- не задан счет
                OR ((category_id IS NOT NULL AND event_id IS NOT NULL) -- обозначено как событие и задана категория
                OR (category_id IS NULL AND event_id IS NULL)) -- не является событием и не задана категория
                OR (tr_t.id != cat_t.id) -- тип категории не соответвует типу транзацкии
                ) AND NOT validated
        """.trimIndent(), Long::class.java)
    }

    @Transactional
    override fun deleteByIdIn(ids: Collection<Long>): Int {
        return if (ids.isNotEmpty()) {
            namedParameterJdbcTemplate.update(
                "DELETE FROM matching.transaction WHERE id IN (:ids) ", MapSqlParameterSource(mapOf("ids" to ids))
            )
        } else {
            0
        }
    }

    @Transactional
    override fun updateSequence() {
        jdbcTemplate.execute("SELECT setval('matching.transaction_id_seq', (SELECT coalesce(MAX(id) + 1, 1) FROM matching.transaction), false)")
    }
}
