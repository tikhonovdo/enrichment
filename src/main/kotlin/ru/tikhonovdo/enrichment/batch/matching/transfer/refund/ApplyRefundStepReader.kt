package ru.tikhonovdo.enrichment.batch.matching.transfer.refund

import org.springframework.batch.item.database.JdbcCursorItemReader
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.math.abs

class ApplyRefundStepReader(dataSource: DataSource): JdbcCursorItemReader<ApplyRefundInfo>() {

    init {
        this.dataSource = dataSource
        sql = """
        SELECT SUM(sum) as sum, refund_for_id, id as source_id
        FROM matching.transaction WHERE refund_for_id IS NOT NULL AND NOT validated
        GROUP BY refund_for_id, id;
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            ApplyRefundInfo(
                sum = BigDecimal.valueOf(abs(rs.getDouble("sum"))),
                refundForId = rs.getLong("refund_for_id"),
                sourceId = rs.getLong("source_id")
            )
        }
    }
}