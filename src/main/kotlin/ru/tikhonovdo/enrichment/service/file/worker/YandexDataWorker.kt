package ru.tikhonovdo.enrichment.service.file.worker

import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransaction
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

@Component
class YandexDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.YANDEX) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        val operations = JSON_MAPPER.readValue(String(content[0]), ListYaTransactionRef())
        return operations.map { toDraftTransaction(it) }
    }

    private fun toDraftTransaction(operation: YaTransaction) = DraftTransaction(
        bankId = Bank.YANDEX.id,
        date = operation.datetime.toLocalDateTime(),
        sum = operation.amount.money?.amount.toString(),
        data = JSON_MAPPER.writeValueAsString(operation)
    )
}

class ListYaTransactionRef: TypeReference<List<YaTransaction>>()

