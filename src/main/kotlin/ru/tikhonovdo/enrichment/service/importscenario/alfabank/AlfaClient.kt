package ru.tikhonovdo.enrichment.service.importscenario.alfabank

import feign.Headers
import feign.RequestLine
import org.springframework.web.bind.annotation.RequestBody
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaOperationsResponse

internal interface AlfaClient {

    @RequestLine("GET /operation-filters/filters")
    fun getFilters(): AlfaFiltersResponse

    @RequestLine("POST /operations-history/operations")
    @Headers("Content-Type: application/json")
    fun getOperations(@RequestBody request: OperationsRequest): AlfaOperationsResponse

}

internal class AlfaFiltersResponse(val accounts: List<AlfaFilterAccount>) {
    class AlfaFilterAccount(val name: String, val number: String)
}

internal class OperationsRequest(
    var size: Int = 20,
    var page: Int = 1,
    var from: String,
    var to: String,
    var filters: List<OperationsRequestFilter> = listOf(),
    var forced: Boolean = false
)
internal class OperationsRequestFilter(
    var values: List<String>,
    var type: FilterType = FilterType.accounts
) {
    enum class FilterType {
        operationSign,
        operationType,
        accounts,
        cards,
        templateType
    }
}