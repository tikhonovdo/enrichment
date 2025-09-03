package ru.tikhonovdo.enrichment.domain.dto.transaction.alfa

class AlfaOperationsResponse(
    var operations: List<AlfaOperation>,
    var pagesInfo: PagesInfo? = null
) {
    class PagesInfo(
        var page: Int,
        var size: Int
    )
}

data class AlfaOperation(
    var amount: Amount,
    var comment: String?,           // Сообщение (актуально для переводов)
    var dateTime: String,           // Дата операции
    var direction: Direction,       // Направление транзакции (дебет/кредит)
    var id: String,
    var title: String,               // Название
    var operationId: String,
    var category: Category,
    var mcc: String? = null,
    var status: String? = null,
    var accountNumber: String? = null,     // не присутствует в ответе API - заполняется в AlfaServiceImpl
    var accountName: String? = null     // не присутствует в ответе API - заполняется в AlfaServiceImpl
) {
    val paymentSum: Double get() {
        val sign = if (direction == Direction.EXPENSE) { -1 } else { 1 }
        return sign * amount.value.toDouble() / amount.minorUnits
    }
}

data class Amount(var value: Long, var currency: String, var minorUnits: Int)
data class Category(var id: String? = null, var name: String? = null)
enum class Direction {
    EXPENSE, // списание
    INCOME // пополнение
}