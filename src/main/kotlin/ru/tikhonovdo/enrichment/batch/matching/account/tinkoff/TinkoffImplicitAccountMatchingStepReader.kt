package ru.tikhonovdo.enrichment.batch.matching.account.tinkoff

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import javax.sql.DataSource

class TinkoffImplicitAccountMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<AccountMatching.Tinkoff>() {
    init {
        this.dataSource = dataSource
        this.sql = """
            WITH t AS (
                SELECT data->>'paymentCurrency' as bank_currency_code,
                       data->>'description' as pattern
                FROM matching.draft_transaction 
                WHERE bank_id = ${Bank.TINKOFF.id} AND (data->>'cardNumber') IS NULL
            ) SELECT DISTINCT ON (t.pattern) t.* FROM t
            LEFT JOIN matching.account_tinkoff at ON t.bank_currency_code = at.bank_currency_code AND t.pattern = at.pattern
            WHERE at.bank_account_code IS NULL;
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            AccountMatching.Tinkoff(
                bankCurrencyCode = rs.getString("bank_currency_code"),
                pattern = rs.getString("pattern")
            )
        }
    }
}