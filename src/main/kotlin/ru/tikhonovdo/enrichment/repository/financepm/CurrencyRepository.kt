package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface CurrencyRepository : JpaRepository<Currency, Long>, BatchRepository<Currency> {
    @Query("SELECT setval('currency_id_seq', (SELECT MAX(id) FROM currency))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE currency CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class CurrencyRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Currency>(
    namedParameterJdbcTemplate,
    "INSERT INTO currency (id, name, short_name, point, available) VALUES (:id, :name, :shortName, :point, :available)"
) {

}