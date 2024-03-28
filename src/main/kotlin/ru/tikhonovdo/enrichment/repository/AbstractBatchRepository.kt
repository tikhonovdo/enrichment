package ru.tikhonovdo.enrichment.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function

interface BatchRepository<T> {
    fun insertBatch(entities: Collection<T>): Int
    fun updateBatch(entities: Collection<T>): Int
}

abstract class AbstractBatchRepository<T>(
    protected val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val insertQuery: String? = null,
    private val updateQuery: String? = null,
    protected open val idExtractor: Function<T, Long?>? = null,
    private val mapper: Function<T, SqlParameterSource>? = null,
): BatchRepository<T> {

    protected val jdbcTemplate: JdbcTemplate = namedParameterJdbcTemplate.jdbcTemplate

    @Transactional
    override fun insertBatch(entities: Collection<T>): Int {
        if (insertQuery != null) {
            return batchQuery(insertQuery, entities)
        }
        throw UnsupportedOperationException("insert batch is not supported")
    }

    @Transactional
    override fun updateBatch(entities: Collection<T>): Int {
        if (updateQuery != null) {
            return batchQuery(updateQuery, entities)
        }
        throw UnsupportedOperationException("update batch is not supported")
    }

    protected open fun batchQuery(query: String, entities: Collection<T>): Int {
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