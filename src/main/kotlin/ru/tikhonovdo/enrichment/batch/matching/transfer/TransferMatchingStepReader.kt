package ru.tikhonovdo.enrichment.batch.matching.transfer

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import javax.sql.DataSource

class TransferMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<TransferMatching>() {
    init {
        this.dataSource = dataSource
        sql = """
            SELECT mt1.name, mt1.id as id_from, mt2.id id_to
            FROM matching.transaction mt1
            JOIN matching.transaction mt2 ON (mt1.date <= mt2.date AND mt2.date < (mt1.date + INTERVAL '5s')) AND mt2.type = 1 AND mt1.sum = mt2.sum
            WHERE mt1.type = 2 AND mt1.category_id IS NULL AND mt2.category_id IS NULL AND mt2.description IS NULL
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