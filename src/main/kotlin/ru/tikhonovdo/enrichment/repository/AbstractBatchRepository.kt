package ru.tikhonovdo.enrichment.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils

interface BatchRepository<T> {
    fun insertBatch(entities: Collection<T>): Int
    fun insertBatchUnsafe(entities: Collection<T>): Int
    fun updateBatch(entities: Collection<T>): Int
}

abstract class AbstractBatchRepository<T>(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val insertQuery: String,
    private val updateQuery: String? = null,
    private val mapper: java.util.function.Function<T, SqlParameterSource>? = null
): BatchRepository<T> {

    protected val jdbcTemplate = namedParameterJdbcTemplate.jdbcTemplate

    override fun insertBatch(entities: Collection<T>): Int {
        return namedParameterJdbcTemplate.batchUpdate(insertQuery, createBatchParams(entities)).size
    }

    override fun insertBatchUnsafe(entities: Collection<T>): Int {
        jdbcTemplate.update("SET session_replication_role = replica")
        val result = insertBatch(entities)
        jdbcTemplate.update("SET session_replication_role = origin")
        return result
    }

    override fun updateBatch(entities: Collection<T>): Int {
        return namedParameterJdbcTemplate.batchUpdate(updateQuery, createBatchParams(entities)).size
    }

    private fun createBatchParams(entities: Collection<T>): Array<out SqlParameterSource> {
        return if (mapper == null) {
            SqlParameterSourceUtils.createBatch(entities)
        } else {
            entities.map { mapper.apply(it) }.toTypedArray()
        }
    }

}