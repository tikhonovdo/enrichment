package ru.tikhonovdo.enrichment.batch.matching.currency

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import javax.sql.DataSource

class TinkoffCurrencyMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<CurrencyMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            SELECT DISTINCT ON (data->>'paymentCurrency') 
                data->>'paymentCurrency' as currency
            FROM matching.draft_transaction
            WHERE bank_id = ${Bank.TINKOFF.id};
        """.trimIndent()
        setRowMapper { rs, _ ->
            CurrencyMatching(
                bankId = Bank.TINKOFF.id,
                bankCurrencyCode = rs.getString("currency")
            )
        }
    }
}