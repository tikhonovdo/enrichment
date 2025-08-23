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
            SELECT data->>'id' as id,
                   id as draft_transaction_id,
                   date,
                   data->>'title' as description,
                   data#>>'{amount,money,amount}' as amount,
                   data#>>'{amount,money,currency}' as amount_currency,
                   data#>>'{statusCode}' as status_code,
                   data->>'description' as category,
                   case
                       when data->>'direction' is not null then data->>'direction'  -- new
                       else data->>'directionV2'
                       end as direction,
                   data->>'comment' as comment
            FROM matching.draft_transaction
            WHERE bank_id = ${Bank.YANDEX.id} 
            AND lower(data->>'id') LIKE 'statement%'
            AND data#>>'{amount,money,amount}' IS NOT NULL
            AND id NOT IN (SELECT draft_transaction_id FROM matching.transaction WHERE draft_transaction_id IS NOT NULL);
        """.trimIndent()
        setRowMapper { rs, _ ->
            YandexRecord(
                id = rs.getString("id"),
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