package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.math.abs

class SearchRefundStepReader(dataSource: DataSource): JdbcCursorItemReader<TransactionMatching>() {

    init {
        this.dataSource = dataSource
        sql = """
            SELECT 
                mt.id, mt.draft_transaction_id, mt.name, mt.description, mt.type, mt.date, mt.sum, mt.account_id
            FROM matching.transaction mt
            WHERE mt.type = ${Type.INCOME.id} 
            AND mt.category_id IS NULL 
            AND length(mt.description) > 0 
            AND mt.event_id IS NULL
            AND mt.refund_for_id IS NULL
            AND mt.validated != TRUE;
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            TransactionMatching(
                id = rs.getLong("id"),
                draftTransactionId = rs.getLong("draft_transaction_id"),
                name = rs.getString("name"),
                description = rs.getString("description"),
                typeId = rs.getLong("type"),
                date = rs.getTimestamp("date").toLocalDateTime(),
                sum = BigDecimal.valueOf(abs(rs.getDouble("sum"))),
                accountId = rs.getLong("account_id"),
                categoryId = null
            )
        }
    }
}