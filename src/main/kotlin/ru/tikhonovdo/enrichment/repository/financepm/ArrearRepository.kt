package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Arrear
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface ArrearRepository : JpaRepository<Arrear, Long>, BatchRepository<Arrear> {
    @Query("SELECT setval('arrear_id_seq', (SELECT MAX(id) FROM arrear))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE arrear CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class ArrearRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Arrear>(
    namedParameterJdbcTemplate,
    "INSERT INTO arrear (id, name, date, balance, account_id, description, available) " +
            "VALUES (:id, :name, :date, :balance, :accountId, :description, :available)",
) {

}
