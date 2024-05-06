package ru.tikhonovdo.enrichment.batch.matching.currency

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import javax.sql.DataSource

class AlfaCurrencyMatchingStepReader(dataSource: DataSource): AbstractCurrencyMatchingStepReader(dataSource, Bank.ALFA)
class TinkoffCurrencyMatchingStepReader(dataSource: DataSource): AbstractCurrencyMatchingStepReader(dataSource, Bank.TINKOFF)

abstract class AbstractCurrencyMatchingStepReader(dataSource: DataSource, bank: Bank): JdbcCursorItemReader<CurrencyMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            SELECT DISTINCT ON (data->>'paymentCurrency') 
                data->>'paymentCurrency' as currency
            FROM matching.draft_transaction
            WHERE bank_id = ${bank.id};
        """.trimIndent()
        setRowMapper { rs, _ ->
            CurrencyMatching(
                bankId = bank.id,
                bankCurrencyCode = rs.getString("currency")
            )
        }
    }
}