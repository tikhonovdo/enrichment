package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import feign.Feign
import feign.Logger
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.DataType
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.RawDataService
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime

interface TinkoffService {
    fun importData(sessionId: String)
}

@Service
class TinkoffServiceImpl(
    @Value("\${import.tinkoff.api-url}") private val tinkoffApiUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val rawDataService: RawDataService
): TinkoffService {

    private val tinkoffClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(TinkoffClient::class.java))
        .logLevel(Logger.Level.FULL)
        .target(TinkoffClient::class.java, tinkoffApiUrl)

    override fun importData(sessionId: String) {
        val start = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.TINKOFF.id)
            .orElse(periodAgo(lastTransactionDefaultPeriod)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = ZonedDateTime.now().toInstant().toEpochMilli()
        val operationsReport = tinkoffClient.getOperationsReport(Format.xls, sessionId, start, end)
        val operationsRaw = tinkoffClient.getOperations(sessionId, start, end)

        rawDataService.saveData(DataType.TINKOFF, content = arrayOf(
            operationsReport.body().asInputStream().readAllBytes(),
            operationsRaw.body().asInputStream().readAllBytes()
        ))
    }
}