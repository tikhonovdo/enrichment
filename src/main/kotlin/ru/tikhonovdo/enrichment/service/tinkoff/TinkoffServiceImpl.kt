package ru.tikhonovdo.enrichment.service.tinkoff

import feign.Feign
import feign.Logger
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.service.worker.TinkoffFileWorker
import java.time.ZoneId
import java.time.ZonedDateTime

interface TinkoffSerivce {
    fun importData(sessionId: String)
}

@Service
class TinkoffServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val tinkoffFileWorker: TinkoffFileWorker
): TinkoffSerivce {

    private val tinkoffClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(TinkoffClient::class.java))
        .logLevel(Logger.Level.FULL)
        .target(TinkoffClient::class.java, "https://www.tinkoff.ru/api/common/v1")

    override fun importData(@RequestParam("sessionId") sessionId: String) {
        val start = transactionRepository.findLastMatchedTransactionByDate().date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = ZonedDateTime.now().toInstant().toEpochMilli()
        val operations = tinkoffClient.getOperations(Format.xls, sessionId, start, end)

        tinkoffFileWorker.saveData(operations.body().asInputStream().readAllBytes())
    }
}