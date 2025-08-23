package ru.tikhonovdo.enrichment.service.file.worker

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaOperation
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.time.LocalDateTime

@Component
class AlfabankDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.ALFA) {

    override fun toDraftTransactionList(json: String): List<DraftTransaction> {
        val operations = JSON_MAPPER.readValue(json, ListAlfaOperationRef())
        val records = mutableListOf<AlfaRecord.Raw>()

        operations.forEach { alfaOperation ->
            AlfaRecord.Raw(alfaOperation.id, alfaOperation.dateTime).apply {
                this.accountName = alfaOperation.accountName
                this.accountNumber = alfaOperation.accountNumber
                this.description = alfaOperation.title.replace(Regex("\\s+"), " ")
                this.paymentSum = alfaOperation.paymentSum
                this.paymentCurrency = alfaOperation.amount.currency
                this.status = alfaOperation.status
                this.category = alfaOperation.category.name
                this.categoryId = alfaOperation.category.id
                this.mcc = alfaOperation.mcc?.toInt().takeIf { it != 0 }
                this.type = alfaOperation.direction.toString()
                this.comment = alfaOperation.comment
            }.let {
                if (it.operationDate.isNotBlank()) {
                    records.add(it)
                }
            }
        }

        val importDate = LocalDateTime.now()
        return records.map { toDraftTransaction(it, importDate) }
    }

    private fun toDraftTransaction(record: AlfaRecord.Raw, importDate: LocalDateTime) = DraftTransaction(
            bankId = Bank.ALFA.id,
            innerBankId = record.id,
            date = AlfaRecord.parseDate(record.operationDate),
            sum = record.paymentSum.toString(),
            data = JSON_MAPPER.writeValueAsString(record),
            importDate = importDate
    )
}

class ListAlfaOperationRef : TypeReference<List<AlfaOperation>>()