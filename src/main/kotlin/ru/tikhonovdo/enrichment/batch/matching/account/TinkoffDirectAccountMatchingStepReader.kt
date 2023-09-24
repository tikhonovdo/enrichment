package ru.tikhonovdo.enrichment.batch.matching.account

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import javax.sql.DataSource

class TinkoffDirectAccountMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<AccountMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = """
            WITH t AS (
                SELECT DISTINCT ON (data->>'cardNumber')
                    data->>'cardNumber' as bank_account_code
                FROM matching.draft_transaction
                WHERE bank_id = ${Bank.TINKOFF.id}
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