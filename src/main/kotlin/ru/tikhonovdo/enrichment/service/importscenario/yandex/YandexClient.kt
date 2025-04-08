package ru.tikhonovdo.enrichment.service.importscenario.yandex

import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import org.springframework.web.bind.annotation.RequestBody
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaOperationRequest

internal interface YandexClient {
    @RequestLine("POST /graphql?operationId={operationId}")
    @Headers("Content-Type: application/json", "Cookie: {cookie}", "Accept-Language: ru")
    fun graphql(
        @RequestBody operation: YaOperationRequest,
        @Param("cookie") cookie: String,
        @Param("operationId") operationId: String,
    ): Response
}