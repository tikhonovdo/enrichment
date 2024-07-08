package ru.tikhonovdo.enrichment.service.file.worker

import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.Operation
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.OperationsCollection
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER

@Component
class YandexDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.YANDEX) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        val operations = JSON_MAPPER.readValue(content[0], OperationsCollection::class.java)
        return operations.items.map { toDraftTransaction(it) }
    }

    private fun toDraftTransaction(operation: Operation) = DraftTransaction(
        bankId = Bank.YANDEX.id,
        date = operation.datetime.toLocalDateTime(),
        sum = operation.money.amount.toString(),
        data = JSON_MAPPER.writeValueAsString(operation)
    )
}

