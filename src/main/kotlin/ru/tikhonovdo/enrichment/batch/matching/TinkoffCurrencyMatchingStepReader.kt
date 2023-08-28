package ru.tikhonovdo.enrichment.batch.matching

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import javax.sql.DataSource

class TinkoffCurrencyMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<CurrencyMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            SELECT DISTINCT ON (items.item->>'paymentCurrency')
                items.item->>'paymentCurrency' as currency
            FROM
                 (SELECT jsonb_array_elements(data) item
                  FROM draft_transaction
                  WHERE bank_id = ${Bank.TINKOFF.id}) items;
        """.trimIndent()
        setRowMapper { rs, _ ->
            CurrencyMatching(
                bankId = Bank.TINKOFF.id,
                bankCurrencyCode = rs.getString("currency")
            )
        }
    }
}