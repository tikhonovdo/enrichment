package ru.tikhonovdo.enrichment.batch.matching

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import javax.sql.DataSource

class TinkoffAccountMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<AccountMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = """
            WITH t as (
                SELECT DISTINCT ON (items.item->>'cardNumber')
                        items.item->>'cardNumber' as bank_account_code
                FROM
                    (SELECT jsonb_array_elements(data) item
                     FROM draft_transaction
                     WHERE bank_id = ${Bank.TINKOFF.id}) items
            ) SELECT bank_account_code FROM t WHERE bank_account_code IS NOT NULL;        
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            AccountMatching(
                bankId = Bank.TINKOFF.id,
                bankAccountCode = rs.getString("bank_account_code")
            )
        }
    }
}