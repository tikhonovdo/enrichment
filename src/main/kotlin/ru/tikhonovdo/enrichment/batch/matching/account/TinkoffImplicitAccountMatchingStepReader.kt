package ru.tikhonovdo.enrichment.batch.matching.account

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import javax.sql.DataSource

class TinkoffImplicitAccountMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<AccountMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = """
            WITH t as (
                SELECT
                    items.item->>'paymentCurrency' as bank_currency_code,
                    items.item->>'description' as pattern,
                    items.item->>'cardNumber' as bank_account_code
                FROM
                    (SELECT jsonb_array_elements(data) item
                     FROM draft_transaction
                     WHERE bank_id = ${Bank.TINKOFF.id}) items
            ) SELECT DISTINCT ON (t.pattern) * FROM t WHERE bank_account_code IS NULL;
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            AccountMatching(
                bankId = Bank.TINKOFF.id,
                bankCurrencyCode = rs.getString("bank_currency_code"),
                pattern = rs.getString("pattern")
            )
        }
    }
}