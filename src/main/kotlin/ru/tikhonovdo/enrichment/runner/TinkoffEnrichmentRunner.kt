package ru.tikhonovdo.enrichment.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.financepm.FinancePmDataExporter
import ru.tikhonovdo.enrichment.processor.TinkoffRecordProcessor
import ru.tikhonovdo.enrichment.tinkoff.TinkoffImporter

@Component
@Order(4)
class TinkoffEnrichmentRunner(
    private val tinkoffImporter: TinkoffImporter,
    private val tinkoffRecordProcessor: TinkoffRecordProcessor,
    private val dataExporter: FinancePmDataExporter
): ApplicationRunner {

    override fun run(appArgs: ApplicationArguments) {
        if (appArgs.containsOption(InitMappingRunner.initMappingOption)) {
            return
        }

        val filePaths = appArgs.nonOptionArgs
        val operationsPaths = filePaths.slice(1 until filePaths.size)
        val records = tinkoffImporter.getRecords(operationsPaths)

        tinkoffRecordProcessor.enrich(records)
        dataExporter.toFile()
    }
}