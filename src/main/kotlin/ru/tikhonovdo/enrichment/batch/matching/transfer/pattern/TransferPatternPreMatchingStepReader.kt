package ru.tikhonovdo.enrichment.batch.matching.transfer.pattern

import org.springframework.batch.item.database.JdbcCursorItemReader
import javax.sql.DataSource

class TransferPatternPreMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<PatternTransferMatchingInfo>() {

    init {
        this.dataSource = dataSource
        sql = "SELECT tp.* FROM matching.transfer_pattern tp"
        this.setRowMapper { rs, _ ->
            PatternTransferMatchingInfo(
                sourceName = rs.getString("source_name"),
                sourceDescription = rs.getString("source_description"),
                sourceType = rs.getLong("source_type"),
                sourceAccountId = rs.getLong("source_account_id"),
                targetAccountId = rs.getLong("target_account_id")
            )
        }
    }
}