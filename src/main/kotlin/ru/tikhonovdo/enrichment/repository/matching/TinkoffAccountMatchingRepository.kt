package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface TinkoffAccountMatchingRepository: BatchRepository<AccountMatching.Tinkoff> {
    fun findAll(): List<AccountMatching.Tinkoff>
    fun findAccountId(bankCurrencyCode: String, pattern: String): Long?
}

@Repository
class TinkoffAccountMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    TinkoffAccountMatchingRepository, AbstractBatchRepository<AccountMatching.Tinkoff>(
        namedParameterJdbcTemplate,
    "INSERT INTO matching.account_tinkoff (bank_account_code, bank_currency_code, pattern) " +
            "VALUES (:bankAccountCode, :bankCurrencyCode, :pattern)",
    ) {

    override fun findAll(): List<AccountMatching.Tinkoff> {
        return namedParameterJdbcTemplate.query("""
            SELECT bank_account_code, bank_currency_code, pattern
            FROM matching.account_tinkoff;
        """.trimIndent()
        ) { rs, _ ->
            AccountMatching.Tinkoff(
                bankAccountCode = rs.getString("bank_account_code"),
                bankCurrencyCode = rs.getString("bank_currency_code"),
                pattern = rs.getString("pattern")
            )
        }
    }

    override fun findAccountId(bankCurrencyCode: String, pattern: String): Long? {
        val result = namedParameterJdbcTemplate.query("""
            SELECT a.account_id FROM matching.account a
            JOIN matching.account_tinkoff at on a.bank_account_code = at.bank_account_code --AND at.bank_account_code IS NOT NULL
            WHERE a.bank_id = :bankId AND at.bank_currency_code = :bankCurrencyCode AND lower(at.pattern) LIKE :pattern
        """.trimIndent(),
            MapSqlParameterSource(mapOf(
                "bankId" to Bank.TINKOFF.id,
                "bankCurrencyCode" to bankCurrencyCode,
                "pattern" to "%${pattern.lowercase()}%"
            ))
        ) { rs, _ -> rs.getLong("account_id") }

        return result.filterNotNull().lastOrNull()
    }
}