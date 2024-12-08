package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.ZonedDateTime

@JsonDeserialize(using = YaOperationsResponseDeserializer::class)
class YaOperationsResponse(
    var operations: OperationsCollection
)

class OperationsCollection(
    var items: List<YaOperation>,
    var cursor: String? = null
)

data class YaOperation(
    var id: String,
    var status: OperationStatus,    // Статус
    var type: String,               // Тип транзакции: пополнение/входящий перевод/покупка/
    var datetime: ZonedDateTime,    // Дата операции
    var name: String,               // Описание
    var listName: String?,          // ???
    var description: String?,        // Категория
    var direction: Direction,       // Направление транзакции (дебет/кредит)
    var cashback: Any?,             // Кэшбэк?
    var money: MoneyObject,         // Сумма и валюта платежа

    @JsonDeserialize(using = CommentDeserializer::class)
    var comment: String?,          // Сообщение (актуально для переводов)
    var additionalInfo: Any?,       // ???
    var splitOperation: Any?        // ???
)

data class OperationStatus(var code: String? = null, var message: String? = null)
data class MoneyObject(var amount: Double? = null, var currency: String? = null)
enum class Direction {
    DEBIT, // списание
    CREDIT // пополнение
}

enum class OperationType {
    PURCHASE,
    REFUND,
    TOPUP,
    TRANSFER_IN,
    TRANSFER_OUT
}

enum class StatusCode {
    CLEAR, // проведена
    HOLD, // средства заблокированы
    CANCEL, // отмена
    FAIL, // не выполнено
}
