package ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.TinkoffRecord
import ru.tikhonovdo.enrichment.util.getNullable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

class TinkoffRecordReader(dataSource: DataSource): JdbcCursorItemReader<TinkoffRecord>() {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    init {
        this.dataSource = dataSource
        sql = """
            SELECT id as draft_transaction_id,
                data->>'operationDate' as operation_date,
                data->>'paymentDate' as payment_date,
                data->>'cardNumber' as card_number,
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
                data->>'roundingForInvestKopilka' as rounding_for_invest_kopilka,
                data->>'sumWithRoundingForInvestKopilka' as sum_with_rounding_for_invest_kopilka
            FROM matching.draft_transaction
            WHERE bank_id = ${Bank.TINKOFF.id} AND (data->>'category') != 'Наличные';
        """.trimIndent()
        setRowMapper { rs, _ ->
            TinkoffRecord(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                operationDate = TinkoffRecord.parseOperationDate(rs.getString("operation_date")),
                paymentDate = rs.getString("payment_date")?.let {
                    dateFormatter.parse(it, LocalDate::from)
                },
                cardNumber = rs.getNullable { it.getString("card_number") },
                status = rs.getString("status"),
                operationSum = rs.getDouble("operation_sum"),
                operationCurrency = rs.getString("operation_currency"),
                paymentSum = rs.getDouble("payment_sum"),
                paymentCurrency = rs.getString("payment_currency"),
                cashback = rs.getNullable { it.getDouble("cashback") },
                category = rs.getString("category"),
                mcc = rs.getNullable { it.getString("mcc") },
                description = rs.getString("description"),
                totalBonuses = rs.getDouble("total_bonuses"),
                roundingForInvestKopilka = rs.getDouble("rounding_for_invest_kopilka"),
                sumWithRoundingForInvestKopilka = rs.getDouble("sum_with_rounding_for_invest_kopilka")
            )
        }
    }
}