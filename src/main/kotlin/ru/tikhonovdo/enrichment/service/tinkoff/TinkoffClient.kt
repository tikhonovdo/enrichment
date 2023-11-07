package ru.tikhonovdo.enrichment.service.tinkoff

import feign.Param
import feign.RequestLine
import feign.Response

interface TinkoffClient {

    @RequestLine("GET /export_operations/?format={format}&sessionid={sessionId}&start={start}&end={end}")
    fun getOperations(@Param("format") format: Format,
                      @Param("sessionId") sessionId: String,
                      @Param("start") start: Long,
                      @Param("end") end: Long): Response

}

enum class Format {
    xls, csv, ofx
}