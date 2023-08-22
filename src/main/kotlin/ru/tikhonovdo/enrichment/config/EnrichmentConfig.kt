package ru.tikhonovdo.enrichment.config

import org.apache.commons.csv.CSVFormat
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tikhonovdo.enrichment.domain.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.old.mapping.AccountMapper
import ru.tikhonovdo.enrichment.old.mapping.CategoryMapper
import ru.tikhonovdo.enrichment.old.processor.TinkoffRecordProcessor
import ru.tikhonovdo.enrichment.old.processor.TransactionProcessor
import ru.tikhonovdo.enrichment.old.processor.TransferProcessor
import ru.tikhonovdo.enrichment.old.runner.TinkoffEnrichmentRunner
import ru.tikhonovdo.enrichment.repository.financepm.CategoryRepository
import ru.tikhonovdo.enrichment.service.MappingService

@Configuration
@ConditionalOnBean(TinkoffEnrichmentRunner::class)
class EnrichmentConfig {

    @Bean
    fun accountMapper(mappingConfig: MappingConfig, csvFormat: CSVFormat) =
        AccountMapper(mappingConfig.accounts, csvFormat)

    @Bean
    fun categoryMapper(mappingConfig: MappingConfig, csvFormat: CSVFormat, financePmDataHolder: FinancePmDataHolder) =
        CategoryMapper(mappingConfig.categories, csvFormat, financePmDataHolder)

    @Bean
    fun transactionProcessor(
        financePmDataHolder: FinancePmDataHolder,
        categoryRepository: CategoryRepository,
        mappingService: MappingService
    ) = TransactionProcessor(financePmDataHolder, categoryRepository, mappingService)

    @Bean
    fun transferProcessor(
        financePmDataHolder: FinancePmDataHolder,
        transactionProcessor: TransactionProcessor,
        mappingService: MappingService
    ) = TransferProcessor(financePmDataHolder, transactionProcessor, mappingService)

    @Bean
    fun tinkoffRecordProcessor(
        transactionProcessor: TransactionProcessor,
        transferProcessor: TransferProcessor
    ) = TinkoffRecordProcessor(transactionProcessor, transferProcessor)

}