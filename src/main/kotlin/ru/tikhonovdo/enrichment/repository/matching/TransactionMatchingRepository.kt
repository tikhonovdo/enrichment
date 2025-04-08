package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.tikhonovdo.enrichment.batch.matching.transfer.complement.TransferComplementInfo
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

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
                OR (tr_t.id != cat_t.id) -- тип категории не соответствует типу транзакции)
            ) OR validated)
        ORDER BY mt.date DESC LIMIT 1
    """, nativeQuery = true)
    fun findLastValidatedTransactionDateByBank(bankId: Long): Optional<LocalDateTime>

    fun existsByDraftTransactionId(draftTransactionId: Long): Boolean

    fun findAllByDateBetweenAndTypeIdEquals(dateStart: LocalDateTime, dateEnd: LocalDateTime, type: Long): List<TransactionMatching>

    @Query("""
        SELECT max(mt.date)
        FROM matching.transaction mt
        JOIN matching.account ma on ma.account_id = mt.account_id
        WHERE ma.bank_id = :bankId
    """, nativeQuery = true)
    fun getLastMatchedDate(bankId: Long): LocalDateTime?

}

interface CustomTransactionMatchingRepository {
    fun setEventIdForTransactions(eventId: Long?, matchingTransactionIds: Collection<Long>)

    fun getUnmatchedTransactionIds(): List<Long>

    fun deleteByIdIn(ids: Collection<Long>): Int

    fun updateSequence()

    fun batchUpdateRefundForId(transactions: Collection<TransactionMatching>)

    fun markValidated(ids: Collection<Long>)

    fun findTransferCandidatesToCreateComplement(transferInfo: TransferComplementInfo): Collection<TransactionMatching>
}

@Repository
class TransactionMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate): CustomTransactionMatchingRepository,
    AbstractBatchRepository<TransactionMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO matching.transaction (name, type, category_id, date, sum, account_id, description, event_id, validated, draft_transaction_id, refund_for_id) " +
                "VALUES (:name, :typeId, :categoryId, :date, :sum, :accountId, :description, :eventId, :validated, :draftTransactionId, :refundForId)",
        "UPDATE matching.transaction SET " +
                "name = :name, type = :typeId, category_id = :categoryId, date = :date, sum = :sum, account_id = :accountId, " +
                "description = :description, event_id = :eventId, validated = :validated, draft_transaction_id = :draftTransactionId, " +
                "refund_for_id = :refundForId WHERE id = :id"
    ) {

    override fun setEventIdForTransactions(eventId: Long?, matchingTransactionIds: Collection<Long>) {
        if (matchingTransactionIds.isNotEmpty()) {
            namedParameterJdbcTemplate.batchUpdate(
                "UPDATE matching.transaction SET event_id = :eventId WHERE id IN (:matchingTransactionIds)",
                arrayOf(MapSqlParameterSource(mapOf("eventId" to eventId, "matchingTransactionIds" to matchingTransactionIds)))
            )
        }
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
                AND mt.refund_for_id IS NULL
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

    override fun markValidated(ids: Collection<Long>) {
        namedParameterJdbcTemplate.update(
            "UPDATE matching.transaction SET validated = true WHERE id IN (:ids) ",
            MapSqlParameterSource(mapOf("ids" to ids))
        )
    }

    override fun batchUpdateRefundForId(transactions: Collection<TransactionMatching>) {
        namedParameterJdbcTemplate.batchUpdate(
            "UPDATE matching.transaction SET refund_for_id = :refundForId WHERE id = :id",
            SqlParameterSourceUtils.createBatch(transactions)
        )
    }

    override fun findTransferCandidatesToCreateComplement(transferInfo: TransferComplementInfo): Collection<TransactionMatching> {
        val sourceName = transferInfo.sourceName
        val sourceDescription = transferInfo.sourceDescription
        val sourceType = transferInfo.sourceType
        val sourceAccountId = transferInfo.sourceAccountId
        val targetAccountId = transferInfo.targetAccountId

        val result = namedParameterJdbcTemplate.query("""
            SELECT * FROM matching.transaction mt1
            WHERE mt1.name like :sourceName 
                AND mt1.description = :sourceDescription 
                AND mt1.type = :sourceType
                AND mt1.account_id = :sourceAccountId
                AND mt1.category_id IS NULL
                AND NOT EXISTS(
                    SELECT * FROM matching.transaction mt2
                    WHERE mt1.name = mt2.name 
                        AND mt1.description = mt2.description
                        AND mt1.date = mt2.date
                        AND mt1.draft_transaction_id = mt2.draft_transaction_id
                        AND mt1.type != mt2.type
                        AND mt2.account_id = :targetAccountId
                )
        """.trimIndent(),
            MapSqlParameterSource(mapOf(
                "sourceName" to "%$sourceName%",
                "sourceDescription" to sourceDescription,
                "sourceType" to sourceType,
                "sourceAccountId" to sourceAccountId,
                "targetAccountId" to targetAccountId
            ))
        ) { rs, _ ->
            TransactionMatching(
                id = rs.getLong("id"),
                draftTransactionId = rs.getLong("draft_transaction_id"),
                name = rs.getString("name"),
                description = rs.getString("description"),
                typeId = rs.getLong("type"),
                date = rs.getTimestamp("date").toLocalDateTime(),
                sum = BigDecimal.valueOf(abs(rs.getDouble("sum"))),
                accountId = rs.getLong("account_id")
            )
        }

        return result.toList()
    }
}
