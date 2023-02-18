package ru.tikhonovdo.enrichment.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.financepm.FinancePmDataExporter
import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.financepm.getDataFilePath
import ru.tikhonovdo.enrichment.processor.TinkoffRecordProcessor
import ru.tikhonovdo.enrichment.runner.InitMappingRunner.Companion.initMappingProfile
import ru.tikhonovdo.enrichment.tinkoff.TinkoffImporter

@Component
@Order(3)
@Profile("!$initMappingProfile")
class TinkoffEnrichmentRunner(
    private val tinkoffImporter: TinkoffImporter,
    private val tinkoffRecordProcessor: TinkoffRecordProcessor,
    private val dataExporter: FinancePmDataExporter,
    private val financePmDataHolder: FinancePmDataHolder
): ApplicationRunner {

    override fun run(appArgs: ApplicationArguments) {
        if (appArgs.containsOption(initMappingProfile)) {
            return
        }

        val filePaths = appArgs.nonOptionArgs
        val operationsPaths = filePaths.slice(1 until filePaths.size)
        val records = tinkoffImporter.getRecords(operationsPaths)

        tinkoffRecordProcessor.enrich(records)
        financePmDataHolder.validate()
        dataExporter.toFile(appArgs.getDataFilePath().parent)
    }
}