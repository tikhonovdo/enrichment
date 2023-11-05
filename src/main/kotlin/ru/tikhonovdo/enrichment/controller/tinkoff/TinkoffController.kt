package ru.tikhonovdo.enrichment.controller.tinkoff

import feign.Feign
import feign.Logger
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.service.worker.TinkoffFileWorker
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
@RequestMapping("/tinkoff")
@Controller
class TinkoffController(
    private val transactionRepository: TransactionRepository,
    private val tinkoffFileWorker: TinkoffFileWorker
) {

    private val tinkoffClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(TinkoffClient::class.java))
        .logLevel(Logger.Level.FULL)
        .target(TinkoffClient::class.java, "https://www.tinkoff.ru/api/common/v1")

    @PostMapping("/import")
    fun importTinkoffData(@RequestParam("sessionId") sessionId: String) {
        val start = transactionRepository.findLastMatchedTransactionByDate().date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = ZonedDateTime.now().toInstant().toEpochMilli()
        val operations = tinkoffClient.getOperations(Format.xls, sessionId, start, end)

        tinkoffFileWorker.saveData(operations.body().asInputStream().readAllBytes())
    }
}