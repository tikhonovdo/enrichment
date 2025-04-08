package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import java.time.LocalDateTime

interface DraftTransactionRepository : JpaRepository<DraftTransaction, Long>,
    BatchRepository<DraftTransaction>, CustomDraftTransactionRepository {
    fun findAllByBankIdAndDate(bankId: Long, date: LocalDateTime): List<DraftTransaction>
}

interface CustomDraftTransactionRepository {
    fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching>

    fun findAllByBankId(bankId: Long): List<DraftTransaction>

    fun findAllByBankIdAndDateBetween(bankId: Long, start: LocalDateTime?, end: LocalDateTime?): List<DraftTransaction>

    fun deleteObsoleteDraft(bank: Bank): Int

    fun getLastUploadDate(bank: Bank): LocalDateTime?
}
@Repository
class DraftTransactionRepositoryImpl(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate
): CustomDraftTransactionRepository, AbstractBatchRepository<DraftTransaction>(
    namedParameterJdbcTemplate,
    "INSERT INTO matching.draft_transaction (bank_id, date, sum, data) VALUES (:bankId, :date, :sum, :data::json)"
) {
    override fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching> {
        return namedParameterJdbcTemplate.query("""
            SELECT DISTINCT ON (items.item->>'category', items.item->>'mcc')
                items.item->>'category' as bankCategoryName,
                items.item->>'mcc' as mcc,
                items.item->>'description' as pattern
            FROM
                 (SELECT jsonb_array_elements(data) item
                  FROM matching.draft_transaction
                  WHERE bank_id = :bankId) items;
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

    override fun findAllByBankId(bankId: Long): List<DraftTransaction> {
        return findAllByBankIdAndDateBetween(bankId, null, null)
    }

    override fun findAllByBankIdAndDateBetween(bankId: Long, start: LocalDateTime?, end: LocalDateTime?): List<DraftTransaction> {
        var betweenCondition = ""
        val params: MutableMap<String, Any> = mutableMapOf("bankId" to bankId)
        if (start != null && end != null) {
            betweenCondition = " AND date BETWEEN :start AND :end"
            params["start"] = start
            params["end"] = end
        }
        return namedParameterJdbcTemplate.query(
            """
                SELECT date, sum, data
                FROM matching.draft_transaction
                WHERE bank_id = :bankId $betweenCondition""".trimIndent(),
            MapSqlParameterSource(params)
        ) { rs, _ ->
            DraftTransaction(
                bankId = bankId,
                date = rs.getTimestamp("date").toLocalDateTime(),
                sum = rs.getString("sum"),
                data = rs.getString("data")
            )
        }
    }

    override fun deleteObsoleteDraft(bank: Bank): Int {
        val deleted = when (bank) {
            Bank.TINKOFF -> deleteObsoleteDraftTinkoff()
            Bank.ALFA -> deleteObsoleteDraftAlfa()
            Bank.YANDEX -> deleteObsoleteDraftYandex()
        }
        jdbcTemplate.execute("SELECT setval('matching.draft_transaction_id_seq', (SELECT coalesce(MAX(id) + 1, 1) FROM matching.draft_transaction), false)")
        return deleted
    }

    private fun deleteObsoleteDraftTinkoff(): Int {
        return jdbcTemplate.update("""
            DELETE FROM matching.draft_transaction
            WHERE bank_id = ${Bank.TINKOFF.id} AND ((data->>'paymentDate') IS NULL OR (data->>'status') != 'OK')
            """.trimIndent())
    }

    private fun deleteObsoleteDraftAlfa(): Int {
        return jdbcTemplate.update("""
            DELETE FROM matching.draft_transaction
            WHERE bank_id = ${Bank.ALFA.id} 
                AND (
                    length(data->>'operationDate') = 10 AND ( -- условие для старого формата записей
                        (data->>'paymentDate') IS NULL -- операция в обработке
                        OR ((data->>'status') != 'Выполнен' AND (data->>'category') != 'Пополнения') -- пополнения выполняются сразу - paymentDate всегда null
                    )
                    OR 
                    length(data->>'operationDate') > 10 AND ( -- условие для нового формата записей 
                        (data->>'operationDate') IS NULL -- операция в обработке
                        OR ((data->>'status') != 'SUCCESS' AND (data->>'category') != 'Пополнения') -- пополнения выполняются сразу - paymentDate всегда null
                    )
                )
            """.trimIndent())
    }

    private fun deleteObsoleteDraftYandex(): Int {
        return jdbcTemplate.update("""
            DELETE FROM matching.draft_transaction
            WHERE bank_id = ${Bank.YANDEX.id} AND (data#>>'{status,code}' = 'CLEAR' OR data#>>'{statusCode}' = 'CLEAR')'
            """.trimIndent())
    }

    override fun getLastUploadDate(bank: Bank): LocalDateTime? {
        var sql = "SELECT max(date) FROM matching.draft_transaction WHERE bank_id = :bankId"
        sql += when (bank) {
            Bank.TINKOFF -> " AND data->>'status' = 'OK'"
            Bank.YANDEX -> " AND (data#>>'{status,code}' = 'CLEAR' OR data#>>'{statusCode}' = 'CLEAR')"
            else -> ""
        }

        return namedParameterJdbcTemplate.queryForObject(sql, MapSqlParameterSource(mapOf("bankId" to bank.id)), LocalDateTime::class.java)
    }
}
