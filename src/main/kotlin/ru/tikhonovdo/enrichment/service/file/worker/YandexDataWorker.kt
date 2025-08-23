package ru.tikhonovdo.enrichment.service.file.worker

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransaction
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.time.LocalDateTime

@Component
class YandexDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.YANDEX) {

    override fun toDraftTransactionList(json: String): List<DraftTransaction> {
        val operations = JSON_MAPPER.readValue(json, ListYaTransactionRef())

        val importDate = LocalDateTime.now()
        return operations.map { toDraftTransaction(it, importDate) }
    }

    private fun toDraftTransaction(operation: YaTransaction, importDate: LocalDateTime) = DraftTransaction(
        bankId = Bank.YANDEX.id,
        innerBankId = operation.id,
        date = operation.datetime.toLocalDateTime(),
        sum = operation.amount.money?.amount.toString(),
        data = JSON_MAPPER.writeValueAsString(operation),
        importDate = importDate
    )
}

class ListYaTransactionRef: TypeReference<List<YaTransaction>>()

