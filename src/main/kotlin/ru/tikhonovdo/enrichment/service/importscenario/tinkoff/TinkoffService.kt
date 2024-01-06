package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import feign.Feign
import feign.Logger
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.FileService
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import java.time.*

interface TinkoffService {
    fun importData(sessionId: String)
}

@Service
class TinkoffServiceImpl(
    @Value("\${import.tinkoff.api-url}") private val tinkoffApiUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val fileService: FileService
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
        val operations = tinkoffClient.getOperations(Format.xls, sessionId, start, end)

        fileService.saveData(operations.body().asInputStream().readAllBytes(), FileType.TINKOFF)
    }
}