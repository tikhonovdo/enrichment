package ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.util.getNullable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.sql.DataSource

class TinkoffRecordReader(dataSource: DataSource): JdbcCursorItemReader<TinkoffRecord>() {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val newDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        this.dataSource = dataSource
        sql = """
            SELECT id as draft_transaction_id,
                date,
                case
                    when data->>'paymentDate' is not null
                    then data->>'paymentDate'
                    else to_timestamp((data->>'debitingTime')::bigint / 1000)::date::text
                end as payment_date,
                data->>'cardNumber' as card_number,
                case
                    when data->>'accountNumber' is not null
                    then data->>'accountNumber'
                    else data->>'account'
                end as account_number,
                data->>'status' as status,
                data->>'operationSum' as operation_sum,
                data->>'operationCurrency' as operation_currency,
                data->>'paymentSum' as payment_sum,
                data->>'paymentCurrency' as payment_currency,
                data->>'cashback' as cashback,
                data->>'category' as category,
                data->>'mcc' as mcc,
                data->>'description' as description,
                data->>'totalBonuses' as total_bonuses,
                data->>'message' as message,
                data->>'brandName' as brand_name,
                case
                    when data->>'type' is not null then upper(data ->>'type')
                else
                    case
                        when (data->>'paymentSum')::numeric > 0 then 'CREDIT' else 'CREDIT'
                    end
                end as type
            FROM matching.draft_transaction 
            WHERE bank_id = ${Bank.TINKOFF.id} 
            AND id NOT IN (SELECT draft_transaction_id FROM matching.transaction WHERE draft_transaction_id IS NOT NULL);
        """.trimIndent()
        setRowMapper { rs, _ ->
            val operationSum = rs.getDouble("operation_sum")
            TinkoffRecord(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                operationDate = rs.getTimestamp("date").toLocalDateTime(),
                paymentDate = rs.getNullable { it.getString("payment_date") }?.let {
                    try {
                        dateFormatter.parse(it, LocalDate::from)
                    } catch (e: DateTimeParseException) {
                        newDateFormatter.parse(it, LocalDate::from)
                    }
                },
                accountNumber = rs.getNullable { it.getString("account_number") },
                cardNumber = rs.getNullable { it.getString("card_number") },
                status = rs.getString("status"),
                operationSum = operationSum,
                operationCurrency = rs.getString("operation_currency"),
                paymentSum = rs.getDouble("payment_sum"),
                paymentCurrency = rs.getString("payment_currency"),
                cashback = rs.getNullable { it.getDouble("cashback") },
                category = rs.getNullable {it.getString("category") }.orEmpty(),
                mcc = rs.getNullable { it.getString("mcc") },
                description = rs.getString("description"),
                totalBonuses = rs.getDouble("total_bonuses"),
                message = rs.getNullable { it.getString("message") },
                brandName = rs.getNullable { it.getString("brand_name") },
                type = TinkoffRecord.Type.valueOf(rs.getString("type"))
            )
        }
    }
}