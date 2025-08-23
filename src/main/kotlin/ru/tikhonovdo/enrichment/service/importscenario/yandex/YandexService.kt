package ru.tikhonovdo.enrichment.service.importscenario.yandex

import feign.Feign
import feign.Logger
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaOperationRequest
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransaction
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.YaTransactionFeedResponse
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.worker.YandexDataWorker
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.time.Period
import java.time.ZoneOffset

interface YandexService {
    fun importData(cookie: String, operationRequest: YaOperationRequest)
}

@Service
class YandexServiceImpl(
    @Value("\${import.yandex.api-url}") private val yandexApiUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val yandexDataWorker: YandexDataWorker
): YandexService {

    private val yandexClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(YandexClient::class.java))
        .logLevel(Logger.Level.FULL)
        .encoder(GsonEncoder())
        .decoder(GsonDecoder())
        .target(YandexClient::class.java, yandexApiUrl)

    override fun importData(cookie: String, operationRequest: YaOperationRequest) {
        val start = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.YANDEX.id)
                .flatMap { transactionMatchingRepository.findMinInvalidTransactionDateAfterLastValidated(Bank.YANDEX, it) }
                .orElseGet {
                    transactionMatchingRepository.getLastImportedDraftDate(Bank.YANDEX.id)
                        .orElse(periodAgo(lastTransactionDefaultPeriod))
                }
                .atZone(ZoneOffset.UTC).toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC) // yandex uses UTC format
                .toZonedDateTime()

        val operationsToSave = mutableListOf<YaTransaction>()
        var cursor: String? = null

        do {
            val request = operationRequest.withCursor(cursor)
            val responseRaw = yandexClient.graphql(request, cookie, request.operationId!!)
            val responseBytes = responseRaw.body().asInputStream().readAllBytes()
            val response = JsonMapper.JSON_MAPPER.readValue(responseBytes, YaTransactionFeedResponse::class.java)
            val neededOperations = response.items.filter {
                it.datetime.isAfter(start)
            }
            cursor = if (neededOperations.isNotEmpty()) {
                operationsToSave.addAll(neededOperations)
                response.cursor
            } else {
                null
            }
        } while (cursor != null)

        yandexDataWorker.saveDrafts(JsonMapper.JSON_MAPPER.writeValueAsString(operationsToSave))
    }

}