package ru.tikhonovdo.enrichment.repository.financepm

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.Account
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface AccountRepository : JpaRepository<Account, Long>, BatchRepository<Account> {
    @Query("SELECT setval('account_id_seq', (SELECT MAX(id) FROM account))", nativeQuery = true)
    fun resetSequence()

    @Modifying(flushAutomatically = true)
    @Query("TRUNCATE TABLE account CASCADE;", nativeQuery = true)
    fun truncate()
}

@Repository
class AccountRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) : AbstractBatchRepository<Account>(
    namedParameterJdbcTemplate,
    "INSERT INTO account (id, name, icon, balance, currency_id, active, is_default, order_id) " +
            "VALUES (:id, :name, :icon, :balance, :currencyId, :active, :default, :orderId)",
) {

}