package ru.tikhonovdo.enrichment.batch.matching.transaction.yandex

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.Direction
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YandexRecord
import ru.tikhonovdo.enrichment.util.getNullable
import javax.sql.DataSource

class YandexRecordReader(dataSource: DataSource): JdbcCursorItemReader<YandexRecord>() {

    init {
        this.dataSource = dataSource
        sql = """
            SELECT id as draft_transaction_id,
                date,
                data->>'name' as description,
                data#>>'{money,amount}' as amount,
                data#>>'{money,currency}' as amount_currency,
                data#>>'{status,code}' as status_code,
                data->>'description' as category,
                data->>'direction' as direction,
                data->>'comment' as comment
            FROM matching.draft_transaction WHERE bank_id = ${Bank.YANDEX.id};
        """.trimIndent()
        setRowMapper { rs, _ ->
            YandexRecord(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                operationDate = rs.getTimestamp("date").toLocalDateTime(),
                paymentDate = rs.getTimestamp("date").toLocalDateTime().toLocalDate(),
                accountName = YandexRecord.ACCOUNT_NAME,
                description = rs.getString("description"),
                paymentSum = rs.getDouble("amount"),
                paymentCurrency = rs.getString("amount_currency"),
                status = rs.getString("status_code"),
                category = rs.getString("category"),
                mcc = null,
                direction = Direction.valueOf(rs.getString("direction")),
                comment = rs.getNullable { it.getString("comment") }
            )
        }
    }
}