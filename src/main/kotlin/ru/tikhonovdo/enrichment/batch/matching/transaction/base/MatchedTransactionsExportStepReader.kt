package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.util.getNullable
import javax.sql.DataSource

class MatchedTransactionsExportStepReader(dataSource: DataSource): JdbcCursorItemReader<TransactionMatching>() {
    init {
        this.dataSource = dataSource
        sql = """                
            SELECT mt.id, mt.name, mt.type, mt.category_id, mt.date, mt.sum, mt.account_id, mt.description, mt.event_id, mt.draft_transaction_id
            FROM matching.transaction mt
            WHERE mt.account_id IS NOT NULL
              AND ((mt.category_id IS NOT NULL AND mt.event_id IS NULL) OR (mt.category_id IS NULL AND mt.event_id IS NOT NULL))
              AND mt.id NOT IN (
                SELECT distinct matching_transaction_id FROM financepm.transaction
                WHERE matching_transaction_id IS NOT NULL
                );
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            TransactionMatching(
                id = rs.getLong("id"),
                draftTransactionId = rs.getLong("draft_transaction_id"),
                name = rs.getString("name"),
                typeId = rs.getLong("type"),
                categoryId = rs.getNullable { it.getLong("category_id") },
                date = rs.getTimestamp("date").toLocalDateTime(),
                sum = rs.getBigDecimal("sum"),
                accountId = rs.getNullable { rs.getLong("account_id") },
                description = rs.getString("description"),
                eventId = rs.getNullable { it.getLong("event_id") }
            )
        }
    }
}