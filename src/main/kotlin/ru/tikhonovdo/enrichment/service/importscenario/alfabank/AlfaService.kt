package ru.tikhonovdo.enrichment.service.importscenario.alfabank

import feign.Feign
import feign.Logger
import feign.gson.GsonDecoder
import feign.gson.GsonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.alfa.AlfaOperation
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.worker.AlfabankDataWorker
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface AlfaService {
    fun importData(cookies: String, xsrf: String)
}

@Service
class AlfaServiceImpl(
    @Value("\${import.alfa.api-url}") private val alfaApiUrl: String,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val alfaDataWorker: AlfabankDataWorker
): AlfaService {

    private fun createClient(cookies: String, xsrf: String): AlfaClient {
        return Feign.builder()
            .client(OkHttpClient())
            .logger(Slf4jLogger(AlfaClient::class.java))
            .logLevel(Logger.Level.FULL)
            .encoder(GsonEncoder())
            .decoder(GsonDecoder())
            .requestInterceptor { it
                .header("Cookie", cookies)
                .header("X-Xsrf-Token", xsrf)
            }
            .target(AlfaClient::class.java, alfaApiUrl)
    }

    override fun importData(cookies: String, xsrf: String) {
        val start = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.ALFA.id)
                .flatMap { transactionMatchingRepository.findMinInvalidTransactionDateAfterLastValidated(Bank.ALFA, it) }
                .orElseGet {
                    transactionMatchingRepository.getLastImportedDraftDate(Bank.ALFA.id)
                            .orElse(periodAgo(lastTransactionDefaultPeriod))
                }

        val end = ZonedDateTime.now().toLocalDate()

        val alfaClient = createClient(cookies, xsrf)
        val accounts = alfaClient.getFilters().accounts.groupBy { it.number }
        val filters = accounts.map { (number, _) -> OperationsRequestFilter(listOf(number)) }

        val operations = mutableListOf<AlfaOperation>()
        filters.forEach { filter ->
            var page = 1
            do {
                val request = OperationsRequest(
                    page = page++,
                    from = DateTimeFormatter.ISO_DATE.format(start),
                    to = DateTimeFormatter.ISO_DATE.format(end),
                    filters = listOf(filter)
                )
                val response = alfaClient.getOperations(request)

                response.operations.forEach { alfaOperation ->
                    run {
                        val accountNumber = filter.values.first()
                        alfaOperation.accountNumber = accountNumber
                        alfaOperation.accountName = accounts[accountNumber]?.first()?.name
                    }
                }
                operations.addAll(response.operations)
            } while (response.operations.isNotEmpty())
        }

        alfaDataWorker.saveDrafts(JsonMapper.JSON_MAPPER.writeValueAsString(operations))
    }

}