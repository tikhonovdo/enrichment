package ru.tikhonovdo.enrichment.service.file.worker

import com.google.gson.Gson
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsDataPayload
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class TinkoffDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.TINKOFF) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        val operations = Gson().fromJson(String(content[0]), TinkoffOperationsDataPayload::class.java).payload
        return operations.map { toDraftTransaction(it) }
    }

    private fun toDraftTransaction(record: TinkoffOperationsRecord) = DraftTransaction(
        bankId = Bank.TINKOFF.id,
        date = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.operationTime), ZoneId.of("UTC")),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )

}