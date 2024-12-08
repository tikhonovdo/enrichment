package ru.tikhonovdo.enrichment.service.file.worker

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaOperationsResponse
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

@Component
class AlfabankDataWorker(
    draftTransactionRepository: DraftTransactionRepository,
): BankDataWorker(draftTransactionRepository, Bank.ALFA) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        val data: AlfaOperationsResponse = JSON_MAPPER.readValue(content[0], AlfaOperationsResponse::class.java)
        val records = mutableListOf<AlfaRecord.Raw>()

        data.operations.forEach { alfaOperation ->
            AlfaRecord.Raw().apply {
                this.operationDate = alfaOperation.dateTime
                this.accountName = alfaOperation.accountName
                this.accountNumber = alfaOperation.accountNumber
                this.description = alfaOperation.title.replace(Regex("\\s+"), " ")
                this.paymentSum = alfaOperation.paymentSum
                this.paymentCurrency = alfaOperation.amount.currency
                this.status = alfaOperation.status
                this.category = alfaOperation.category.name
                this.mcc = alfaOperation.mcc?.toInt().takeIf { it != 0 }
                this.type = alfaOperation.direction.toString()
                this.comment = alfaOperation.comment
            }.let {
                if (!it.operationDate.isNullOrBlank()) {
                    records.add(it)
                }
            }
        }

        return records.map { toDraftTransaction(it) }
    }

    private fun toDraftTransaction(record: AlfaRecord.Raw) = DraftTransaction(
        bankId = Bank.ALFA.id,
        date = AlfaRecord.parseDate(record.operationDate!!),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )
}