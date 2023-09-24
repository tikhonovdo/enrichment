package ru.tikhonovdo.enrichment.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils

interface BatchRepository<T> {
    fun insertBatch(entities: Collection<T>): Int
    fun updateBatch(entities: Collection<T>): Int
}

abstract class AbstractBatchRepository<T>(
    protected val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val insertQuery: String? = null,
    private val updateQuery: String? = null,
    protected open val idExtractor: java.util.function.Function<T, Long?>? = null,
    private val mapper: java.util.function.Function<T, SqlParameterSource>? = null,
): BatchRepository<T> {

    protected val jdbcTemplate: JdbcTemplate = namedParameterJdbcTemplate.jdbcTemplate

    override fun insertBatch(entities: Collection<T>): Int {
        if (insertQuery != null) {
            return batchQuery(insertQuery, entities)
        }
        throw UnsupportedOperationException("insert batch is not supported")
    }

    override fun updateBatch(entities: Collection<T>): Int {
        if (updateQuery != null) {
            return batchQuery(updateQuery, entities)
        }
        throw UnsupportedOperationException("update batch is not supported")
    }

    protected fun batchQuery(query: String, entities: Collection<T>): Int {
        return namedParameterJdbcTemplate.batchUpdate(query, createBatchParams(entities)).size
    }

    private fun createBatchParams(entities: Collection<T>): Array<out SqlParameterSource> {
        return if (mapper == null) {
            SqlParameterSourceUtils.createBatch(entities)
        } else {
            entities.map { mapper.apply(it) }.toTypedArray()
        }
    }

}