package ru.tikhonovdo.enrichment.batch.matching.currency

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import javax.sql.DataSource

class CurrencyMatchingStepReader(dataSource: DataSource, bank: Bank, sql: String = defaultSql(bank)): JdbcCursorItemReader<CurrencyMatching>() {
    init {
        this.dataSource = dataSource
        this.sql = sql
        setRowMapper { rs, _ ->
            CurrencyMatching(
                bankId = bank.id,
                bankCurrencyCode = rs.getString("currency")
            )
        }
    }
}

private fun defaultSql(bank: Bank) =
    """
        SELECT DISTINCT ON (data->>'paymentCurrency') 
            data->>'paymentCurrency' as currency
        FROM matching.draft_transaction
        WHERE bank_id = ${bank.id} and (data->>'paymentCurrency') is not null;
    """.trimIndent()