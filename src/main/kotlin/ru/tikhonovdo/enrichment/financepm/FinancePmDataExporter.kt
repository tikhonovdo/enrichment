package ru.tikhonovdo.enrichment.financepm

import com.beust.klaxon.Klaxon
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.config.OutputFileConfig
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class FinancePmDataExporter(
    private val klaxon: Klaxon,
    private val outputFileConfig: OutputFileConfig,
    private val financePmDataHolder: FinancePmDataHolder
) {

    private val exportDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(outputFileConfig.datePattern)
    private fun LocalDate.toExportFormat(): String = exportDateFormatter.format(this)

    fun toFile() {
        val outputPath = outputFileConfig.name.replace("{date}", LocalDate.now().toExportFormat())

        if (financePmDataHolder.data.isEmpty) {
            throw IllegalStateException("FinancePM data is empty")
        } else {
            File(outputPath).writeText(klaxon.toJsonString(financePmDataHolder.data))
        }
    }

}
