package ru.tikhonovdo.enrichment.old.tinkoff

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.old.runner.TinkoffEnrichmentRunner
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.bufferedReader

@Component
@ConditionalOnBean(TinkoffEnrichmentRunner::class)
class TinkoffImporter(private val csvFormat: CSVFormat) {

    fun getRecords(operationsPaths: List<String>): List<TinkoffRecord> {
        if (operationsPaths.isEmpty()) {
            log.warn("No paths to operations files specified")
        }
        return operationsPaths.map { getRecords(it) }.flatten()
    }

    private fun getRecords(operationsPath: String): List<TinkoffRecord> {
        val operationsCsv = CSVParser(Paths.get(operationsPath).bufferedReader(), csvFormat)
        val rows = mutableListOf<TinkoffRecord>()
        for (csvRecord in operationsCsv) {
            val operationDate = csvRecord[0]
            val paymentDate = csvRecord.get("paymentDate").ifBlank { operationDate.split(" ")[0] }
            val row = TinkoffRecord(
                dateTimeFormatter.parse(operationDate, LocalDateTime::from),
                paymentDate.toLocalDate(),
                csvRecord.get("cardNumber"),
                csvRecord.get("status"),
                csvRecord.get("operationSum").parseAsDouble(),
                csvRecord.get("operationCurrency"),
                csvRecord.get("paymentSum").parseAsDouble(),
                csvRecord.get("paymentCurrency"),
                csvRecord.get("cashback").ifBlank { "0" }.toInt(),
                csvRecord.get("category"),
                csvRecord.get("mcc"),
                csvRecord.get("description"),
                csvRecord.get("totalBonuses").parseAsDouble(),
                csvRecord.get("roundingForInvestKopilka").parseAsDouble(),
                csvRecord.get("sumWithRoundingForInvestKopilka").parseAsDouble()
            )
            rows.add(row)
        }
        return rows.sortedBy { it.operationDate }
    }

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy[ HH:mm[:ss]]")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private fun String.parseAsDouble() = this.ifEmpty { "0" }.replace(',','.').toDouble()
    private fun String.toLocalDate(): LocalDate = dateFormatter.parse(this, LocalDate::from)

    companion object {
        private val log = LoggerFactory.getLogger(TinkoffImporter::class.java)
    }
}