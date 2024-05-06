package ru.tikhonovdo.enrichment.batch.matching.account

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import javax.sql.DataSource

class AlfaAccountMatchingStepReader(dataSource: DataSource): AbstractAccountMatchingStepReader(dataSource, Bank.ALFA, "accountNumber")
class TinkoffDirectAccountMatchingStepReader(dataSource: DataSource): AbstractAccountMatchingStepReader(dataSource, Bank.TINKOFF, "accountNumber")

abstract class AbstractAccountMatchingStepReader(dataSource: DataSource, bank: Bank, fieldName: String): JdbcCursorItemReader<AccountMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = """
            WITH t AS (
                SELECT DISTINCT ON (data->>'$fieldName')
                    data->>'$fieldName' as bank_account_code
                FROM matching.draft_transaction
                WHERE bank_id = ${bank.id}
            ) SELECT bank_account_code FROM t WHERE bank_account_code IS NOT NULL;        
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            AccountMatching(
                bankId = bank.id,
                bankAccountCode = rs.getString("bank_account_code")
            )
        }
    }
}