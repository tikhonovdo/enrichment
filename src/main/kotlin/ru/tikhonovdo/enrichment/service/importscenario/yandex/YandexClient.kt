package ru.tikhonovdo.enrichment.service.importscenario.yandex

import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import org.springframework.web.bind.annotation.RequestBody

@Headers("Cookie: {cookie}")
internal interface YandexClient {
    @RequestLine("POST /graphql")
    @Headers("Content-Type: application/json")
    fun graphql(
        @Param("cookie") cookie: String,
        @RequestBody operations: GraphQLRequest,
    ): Response
}

internal class GraphQLRequest(
    val variables: Variables = Variables(),
    val operationName: String = "OperationsFeed"
) {

    val query = "query OperationsFeed(\$pageSize: Int!, \$cursor: String, \$product: AgreementProduct, " +
                "\$agreementId: String, \$skipPendingOperations: Boolean!) { \n bankUser { \n operations(" +
                "\n pageSize: \$pageSize \n cursor: \$cursor \n product: \$product \n agreementId: \$agreementId \n ) " +
                "{ \n  cursor \n items { \n ...operationFieldsForList \n } \n } \n pendingOperations(product: \$product, agreementId: \$agreementId) " +
                "@skip(if: \$skipPendingOperations) { \n title \n cursor \n items { \n ...operationFieldsForList \n __typename \n } " +
                "\n __typename \n } \n } \n } \n fragment operationFieldsForList on Operation { \n id \n status { \n code \n message \n } " +
                "\n type \n datetime \n name \n description \n direction \n money { \n amount \n currency \n } \n " +
                "comment { \n text \n } \n }"
    class Variables(
        val cursor: String? = null,
        val pageSize: Int = 40,
        val skipPendingOperations: Boolean = true
    )
}