package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Arrear
import java.util.function.Function

interface ArrearRepository : JpaRepository<Arrear, Long>, FinancePmRepository<Arrear>

@Repository
class ArrearRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<Arrear>(
    namedParameterJdbcTemplate,
    "financepm.arrear",
    Function { it.id },
    "INSERT INTO financepm.arrear (id, name, date, balance, account_id, description, available) " +
            "VALUES (:id, :name, :date, :balance, :accountId, :description, :available)",
)
