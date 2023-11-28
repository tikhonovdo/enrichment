package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Currency
import ru.tikhonovdo.enrichment.repository.BatchRepository
import java.util.function.Function

interface CurrencyRepository : JpaRepository<Currency, Long>, FinancePmRepository<Currency>

@Repository
class CurrencyRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<Currency>(
    namedParameterJdbcTemplate,
    "financepm.currency",
    Function { it.id },
    "INSERT INTO financepm.currency (id, name, short_name, point, available) VALUES (:id, :name, :shortName, :point, :available)"
)
