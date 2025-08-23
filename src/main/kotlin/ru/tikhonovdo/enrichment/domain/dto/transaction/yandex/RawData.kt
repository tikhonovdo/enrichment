package ru.tikhonovdo.enrichment.domain.dto.transaction.yandex

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.tikhonovdo.enrichment.domain.Type
import java.time.ZonedDateTime

data class YaOperationRequest(
    var operationName: String,
    var extensions: Extensions?,
    var variables: Variables? = null
) {

    val operationId: String?
        get() = extensions?.persistedQuery?.sha256Hash

    data class Extensions(
        var persistedQuery: PersistedQuery?
    )

    data class PersistedQuery(
        var version: Int = 1,
        var sha256Hash: String?
    )

    data class Variables(
        val size: Int = 30,
        val cursor: String?,
        val filterType: String? = "PAY_CARD",
        val agreementId: String?
    )

    fun withCursor(cursor: String?): YaOperationRequest {
        this.variables = this.variables?.copy(cursor = cursor)
        return this
    }

}

@JsonDeserialize(using = YaTransactionFeedResponseDeserializer::class)
class YaTransactionFeedResponse(
    var items: List<YaTransaction>,
    var cursor: String? = null,
    var isEmptyByFilter: Boolean? = null
)

data class YaTransaction(
    var id: String,
    var statusCode: String,    // Статус
    @JsonProperty("date")
    var datetime: ZonedDateTime,    // Дата операции
    @JsonProperty("title")
    @JsonDeserialize(using = TitleDeserializer::class)
    var name: String,               // Описание
    @JsonProperty("description")
    var category: String?,        // Категория
    var direction: Direction,       // Направление транзакции (дебет/кредит)
    var amount: Amount,             // Сумма и валюта платежа
    var comment: String?,          // Сообщение (актуально для переводов)
)

data class Amount(var money: Money? = null, var plus: String? = null)
data class Money(var amount: Double? = null, var currency: String? = null)
enum class Direction {
    DEBIT, // списание
    CREDIT; // пополнение

    fun toDomainType(): Type {
        return when (this) {
            CREDIT -> Type.INCOME
            DEBIT -> Type.OUTCOME
        }
    }
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
