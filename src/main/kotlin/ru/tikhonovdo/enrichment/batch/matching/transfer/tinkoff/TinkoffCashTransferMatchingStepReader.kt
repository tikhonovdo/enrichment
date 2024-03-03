package ru.tikhonovdo.enrichment.batch.matching.transfer.tinkoff

import org.springframework.batch.item.database.JdbcCursorItemReader
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.Event
import ru.tikhonovdo.enrichment.domain.Type
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.math.abs

class TinkoffCashTransferMatchingStepReader(dataSource: DataSource): JdbcCursorItemReader<TransactionMatching>() {

    init {
        this.dataSource = dataSource
        sql = """
            SELECT dt.id as draft_transaction_id,
                   a.account_id,
                   dt.data->>'operationDate' as operation_date,
                   dt.data->>'paymentSum' as payment_sum,
                   dt.data->>'description' as description
            FROM matching.draft_transaction dt
            JOIN matching.account a ON a.bank_account_code = (dt.data->>'cardNumber') AND a.bank_id = ${Bank.TINKOFF.id}
            WHERE dt.bank_id = ${Bank.TINKOFF.id} AND (dt.data->>'category') = 'Наличные' AND (dt.data->>'status') = 'OK';
        """.trimIndent()
        this.setRowMapper { rs, _ ->
            TransactionMatching(
                draftTransactionId = rs.getLong("draft_transaction_id"),
                name = rs.getString("description"),
                typeId = Type.OUTCOME.id,
                categoryId = null,
                eventId = Event.TRANSFER.id,
                date = TinkoffRecord.parseOperationDate(rs.getString("operation_date")),
                sum = BigDecimal.valueOf(abs(rs.getDouble("payment_sum"))),
                accountId = rs.getLong("account_id")
            )
        }
    }
}