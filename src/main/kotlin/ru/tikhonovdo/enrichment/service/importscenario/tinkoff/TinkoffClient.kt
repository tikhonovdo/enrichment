package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import feign.Param
import feign.RequestLine
import feign.Response

internal interface TinkoffClient {

    @RequestLine("GET /export_operations/?format={format}&sessionid={sessionId}&start={start}&end={end}")
    fun getOperationsReport(@Param("format") format: Format,
                            @Param("sessionId") sessionId: String,
                            @Param("start") start: Long,
                            @Param("end") end: Long): Response

    @RequestLine("GET /operations/?sessionid={sessionId}&start={start}&end={end}")
    fun getOperations(@Param("sessionId") sessionId: String,
                      @Param("start") start: Long,
                      @Param("end") end: Long): Response
}

enum class Format {
    xls, csv, ofx
}