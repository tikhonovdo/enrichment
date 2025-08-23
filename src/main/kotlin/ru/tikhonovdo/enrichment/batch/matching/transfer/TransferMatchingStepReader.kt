package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import javax.sql.DataSource

class TransferMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<TransferMatching>() {
    init {
        this.dataSource = dataSource
        // Необходимо смотреть на интервал -2..+5 сек от совершения транзакции, т.к. из-за разницы во времени между банками
        // может быть выглядеть так, что деньги пришли раньше, чем ушли.
        sql = """
            SELECT mt1.name, mt1.id as id_from, mt2.id id_to
            FROM matching.transaction mt1           
                JOIN matching.transaction mt2 ON ((mt1.date - INTERVAL '2s') < mt2.date AND mt2.date < (mt1.date + INTERVAL '5s')) AND mt2.type = ${Type.INCOME.id}
                JOIN financepm.account a1 ON mt1.account_id = a1.id
                JOIN financepm.account a2 ON mt2.account_id = a2.id
            WHERE mt1.type = ${Type.OUTCOME.id}
              AND mt1.category_id IS NULL AND mt2.category_id IS NULL
              AND mt1.event_id is null AND mt2.event_id is null
              AND NOT mt1.validated AND NOT mt2.validated
              AND CASE WHEN (a1.currency_id = a2.currency_id) THEN (mt1.sum = mt2.sum) ELSE TRUE END
            ORDER BY mt1.date;
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            TransferMatching(
                name = rs.getString("name"),
                matchingTransactionIdFrom = rs.getLong("id_from"),
                matchingTransactionIdTo = rs.getLong("id_to")
            )
        }
    }
}