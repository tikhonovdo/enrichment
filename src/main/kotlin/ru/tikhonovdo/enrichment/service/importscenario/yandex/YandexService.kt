package ru.tikhonovdo.enrichment.service.importscenario.yandex

import feign.Feign
import feign.Logger
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.DataType
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.Operation
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.OperationsCollection
import ru.tikhonovdo.enrichment.domain.dto.transaction.yandex.OperationsResponse
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.RawDataService
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.time.Period
import java.time.ZoneOffset

interface YandexService {
    fun importData(cookie: String)
}

@Service
class YandexServiceImpl(
    @Value("\${import.yandex.url}") private val yandexApiUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val rawDataService: RawDataService
): YandexService {

    private val yandexClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(YandexClient::class.java))
        .logLevel(Logger.Level.FULL)
        .encoder(GsonEncoder())
        .target(YandexClient::class.java, yandexApiUrl)

    override fun importData(cookie: String) {
        val fromDateTime = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.YANDEX.id)
            .orElse(periodAgo(lastTransactionDefaultPeriod))
            .atZone(ZoneOffset.UTC).toOffsetDateTime()
            .withOffsetSameInstant(ZoneOffset.UTC) // yandex uses UTC format
            .toZonedDateTime()

        val operationsToSave = mutableListOf<Operation>()
        var cursor: String? = null

        do {
            val variables = GraphQLRequest.Variables(cursor)
            val response = yandexClient.graphql(cookie, GraphQLRequest(variables))
            val responseBytes = response.body().asInputStream().readAllBytes()
            val operations = JsonMapper.JSON_MAPPER.readValue(responseBytes, OperationsResponse::class.java).operations
            val neededOperations = operations.items.filter {
                it.datetime.isAfter(fromDateTime)
            }
            cursor = if (neededOperations.isNotEmpty()) {
                operationsToSave.addAll(neededOperations)
                operations.cursor
            } else {
                null
            }
        } while (cursor != null)

        rawDataService.saveData(DataType.YANDEX, content = arrayOf(
            JsonMapper.JSON_MAPPER.writeValueAsString(OperationsCollection(operationsToSave)).toByteArray()
        ))
    }

}