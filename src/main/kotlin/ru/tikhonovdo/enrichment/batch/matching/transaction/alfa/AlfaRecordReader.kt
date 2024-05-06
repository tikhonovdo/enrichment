package ru.tikhonovdo.enrichment.batch.matching.transaction.alfa

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.AlfaRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

class AlfaRecordReader(dataSource: DataSource): JdbcCursorItemReader<AlfaRecord>() {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    init {
        this.dataSource = dataSource
        sql = """
            SELECT id as draft_transaction_id,
                date as operation_date,
                data->>'paymentDate' as payment_date,
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
            FROM matching.draft_transaction WHERE bank_id = ${Bank.ALFA.id};
        """.trimIndent()
        setRowMapper { rs, _ ->
            AlfaRecord(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                operationDate = AlfaRecord.parseOperationDate(rs.getString("operation_date")),
                paymentDate = rs.getString("payment_date")?.takeIf { it.isNotBlank() }?.let {
                    dateFormatter.parse(it, LocalDate::from)
                },
                accountName = rs.getString("account_name"),
                accountNumber = rs.getString("account_number"),
                cardName = rs.getString("card_name"),
                cardNumber = rs.getString("card_number"),
                description = rs.getString("description"),
                paymentSum = rs.getDouble("payment_sum"),
                paymentCurrency = rs.getString("payment_currency"),
                status = rs.getString("status"),
                category = rs.getString("category"),
                mcc = rs.getInt("mcc").let {
                    return@let if (it == 0) null else it
                }?.toString(),
                type = rs.getString("type"),
                comment = rs.getString("comment")
            )
        }
    }
}