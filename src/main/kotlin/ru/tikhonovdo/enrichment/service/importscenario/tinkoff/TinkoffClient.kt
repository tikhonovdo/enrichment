package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import feign.Param
import feign.RequestLine
import feign.Response
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsDataPayload
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffReceiptData

interface TinkoffClient {

    @Deprecated("get rid of this method -- stop using xls report as primary source")
    @RequestLine("GET /export_operations/?format=xls&sessionid={sessionId}&start={start}&end={end}")
    fun getOperationsReport(@Param("sessionId") sessionId: String,
                            @Param("start") start: Long,
                            @Param("end") end: Long): Response

    // todo: make this method the only data source - get rid of getOperationsReport
    @RequestLine("GET /operations/?sessionid={sessionId}&start={start}&end={end}")
    fun getOperations(@Param("sessionId") sessionId: String,
                      @Param("start") start: Long,
                      @Param("end") end: Long): TinkoffOperationsDataPayload

    @RequestLine("GET /shopping_receipt?operationId={operationId}&account={account}&sessionid={sessionId}")
    fun getReceiptData(@Param("operationId") operationId: String,
                       @Param("account") account: String,
                       @Param("sessionId") sessionId: String): TinkoffReceiptData

}