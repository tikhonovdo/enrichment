package ru.tikhonovdo.enrichment.service.importscenario.tinkoff

import feign.Feign
import feign.Logger
import feign.codec.DecodeException
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tikhonovdo.enrichment.config.ImportDataProperties
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsDataPayload
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsRecord
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffReceiptData
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.service.file.worker.TinkoffDataWorker
import ru.tikhonovdo.enrichment.service.importscenario.periodAgo
import ru.tikhonovdo.enrichment.util.JsonMapper
import java.time.Period
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.function.Function

interface TinkoffService {
    fun importData(sessionId: String)
}

@Service
class TinkoffServiceImpl(
    tinkoffProperties: ImportDataProperties,
    @Value("\${import.last-transaction-default-period}") private val lastTransactionDefaultPeriod: Period,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val tinkoffDataWorker: TinkoffDataWorker
): TinkoffService {

    private val tinkoffClient = Feign.builder()
        .client(OkHttpClient())
        .logger(Slf4jLogger(TinkoffClient::class.java))
        .logLevel(Logger.Level.FULL)
        .encoder(JacksonEncoder())
        .decoder(JacksonDecoder())
        .target(TinkoffClient::class.java, tinkoffProperties.apiUrl)

    override fun importData(sessionId: String) {
        val start = transactionMatchingRepository.findLastValidatedTransactionDateByBank(Bank.TINKOFF.id)
                .flatMap { transactionMatchingRepository.findMinInvalidTransactionDateAfterLastValidated(Bank.TINKOFF, it) }
                .orElseGet {
                    transactionMatchingRepository.getLastImportedDraftDate(Bank.TINKOFF.id)
                            .orElse(periodAgo(lastTransactionDefaultPeriod))
                }
                .toInstant(ZoneOffset.UTC).toEpochMilli() + 1

        val end = ZonedDateTime.now().toInstant().toEpochMilli()

        val operationsRaw = tinkoffClient.getOperations(sessionId, start, end)
        if (operationsRaw.payload.filter { it.hasReceipt }.size < 25) {
            tryToLoadReceipts(operationsRaw) { tinkoffClient.getReceiptData(it.authorizationId!!, it.account, sessionId) }
        }

        tinkoffDataWorker.saveDrafts(JsonMapper.JSON_MAPPER.writeValueAsString(operationsRaw))
    }

    fun tryToLoadReceipts(data: TinkoffOperationsDataPayload, receiptSupplier: Function<TinkoffOperationsRecord, TinkoffReceiptData>) {
        try {
            data.payload.forEach {
                if (it.hasReceipt) {
                    it.receipt = receiptSupplier.apply(it)
                    Thread.sleep(500) // trying to prevent REQUEST_RATE_LIMIT_EXCEEDED
                }
            }
        } catch (ignore: DecodeException) {
            // this is non-critical data, so there is no need to abort import
            // due to errors here (it will be logged by feign)
        }
    }
}