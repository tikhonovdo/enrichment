package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Account
import java.util.function.Function

interface AccountRepository : JpaRepository<Account, Long>, FinancePmRepository<Account> {

    @Query("SELECT a1.id FROM financepm.account a1" +
            " LEFT JOIN financepm.account a2 ON a1.currency_id = a2.currency_id" +
            " WHERE a2.id = :accountId AND lower(a1.name) like '%cash%'", nativeQuery = true)
    fun findCashAccountIdByAccountId(accountId: Long): Long?
}

@Repository
class AccountRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractFinancePmRepository<Account>(
    namedParameterJdbcTemplate,
    "financepm.account",
    Function { it.id },
    "INSERT INTO financepm.account (id, name, icon, balance, currency_id, active, is_default, order_id) " +
            "VALUES (:id, :name, :icon, :balance, :currencyId, :active, :default, :orderId)",
)
