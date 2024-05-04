package ru.tikhonovdo.enrichment.repository.financepm

import jakarta.transaction.Transactional
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

sealed interface FinancePmRepository<T>: BatchRepository<T>, CustomFinancePmRepository<T> {
    fun findAll(): List<T>
    fun count(): Long
}

interface CustomFinancePmRepository<T> {
    fun saveDataFromScratch(entities: Collection<T>): Int
    @Deprecated(message = "not used")
    fun saveBatch(entities: Collection<T>): Int
    fun updateSequence()
}

abstract class AbstractFinancePmRepository<T>(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val tableName: String,
    override val idExtractor: java.util.function.Function<T, Long?>,
    insertQuery: String,
    updateQuery: String? = null
): AbstractBatchRepository<T>(namedParameterJdbcTemplate, insertQuery, updateQuery, idExtractor), CustomFinancePmRepository<T> {

    private val insertQueryWithoutId = insertQuery.replace(Regex("\\((:)*id, "), "(")

    private fun truncate() {
        jdbcTemplate.update("TRUNCATE TABLE $tableName CASCADE")
    }

    override fun updateSequence() {
        jdbcTemplate.execute("SELECT setval('${tableName}_id_seq', (SELECT coalesce(MAX(id) + 1, 1) FROM $tableName), false)")
    }

    @Transactional
    override fun saveDataFromScratch(entities: Collection<T>): Int {
        truncate()
        jdbcTemplate.update("SET session_replication_role = replica")
        val count = insertBatch(entities)
        jdbcTemplate.update("SET session_replication_role = origin")
        updateSequence()
        return count
    }

    @Deprecated("not used")
    @Transactional
    override fun saveBatch(entities: Collection<T>): Int {
        val withId = entities.filter { idExtractor.apply(it) != null }
        val withoutId = entities.filter { idExtractor.apply(it) == null }
        var result = 0
        if (withoutId.isNotEmpty()) {
            result += batchQuery(insertQueryWithoutId, entities)
        }
        if (withId.isNotEmpty()) {
            result += updateBatch(withId)
        }
        return result
    }
}