package ru.tikhonovdo.enrichment.batch.matching.transaction.alfa

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaRecord
import ru.tikhonovdo.enrichment.util.getNullable
import javax.sql.DataSource

class AlfaRecordReader(dataSource: DataSource, thresholdDate: LocalDateTime): JdbcCursorItemReader<AlfaRecord>() {

    private val operationDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]")

    init {
        this.dataSource = dataSource
        sql = """
            SELECT id as draft_transaction_id,
                data->>'operationDate' as operation_date,
                data->>'cardNumber' as card_number,
                data->>'status' as status,
                data->>'paymentSum' as payment_sum,
                data->>'paymentCurrency' as payment_currency,
                data->>'accountNumber' as account_number,
                data->>'accountName' as account_name,
                data->>'cardName' as card_name,
                data->>'category' as category,
                data->>'mcc' as mcc,
                data->>'description' as description,
                data->>'type' as type,
                data->>'comment' as comment
            FROM matching.draft_transaction 
            WHERE bank_id = ${Bank.ALFA.id} AND date > '${operationDateFormatter.format(thresholdDate)}';
        """.trimIndent()
        setRowMapper { rs, _ ->
            AlfaRecord(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                operationDate = AlfaRecord.parseDate(rs.getString("operation_date")),
                accountName = rs.getString("account_name"),
                accountNumber = rs.getString("account_number"),
                cardName = rs.getNullable { it.getString("card_name") },
                cardNumber = rs.getNullable { it.getString("card_number") },
                description = rs.getString("description"),
                paymentSum = rs.getDouble("payment_sum"),
                paymentCurrency = rs.getString("payment_currency"),
                status = rs.getNullable { it.getString("status") },
                category = rs.getString("category"),
                mcc = rs.getInt("mcc").let {
                    return@let if (it == 0) null else it
                }?.toString(),
                type = AlfaRecord.Type.valueOf(rs.getString("type").uppercase()),
                comment = rs.getNullable { it.getString("comment") }.orEmpty()
            )
        }
    }
}