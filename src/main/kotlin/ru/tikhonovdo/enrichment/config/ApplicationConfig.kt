package ru.tikhonovdo.enrichment.config

import org.apache.commons.csv.CSVFormat
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.mapping.AccountMapper
import ru.tikhonovdo.enrichment.mapping.CategoryMapper
import ru.tikhonovdo.enrichment.mapping.TransactionMapper
import ru.tikhonovdo.enrichment.processor.TransactionProcessor
import ru.tikhonovdo.enrichment.processor.TransferProcessor

@Configuration
class ApplicationConfig {

    @Bean
    fun csvFormat(): CSVFormat = CSVFormat.DEFAULT.builder()
        .setIgnoreHeaderCase(true)
        .setHeader().setSkipHeaderRecord(true)
        .setDelimiter(';')
        .build()

    @Bean
    @ConfigurationProperties(prefix = "mapping")
    fun mappingConfig() = MappingConfig()

    @Bean
    @ConfigurationProperties(prefix = "output-file")
    fun outputFileConfig() = OutputFileConfig()

    @Bean
    fun accountMapper(mappingConfig: MappingConfig, csvFormat: CSVFormat) =
        AccountMapper(mappingConfig.accounts, csvFormat)

    @Bean
    fun categoryMapper(mappingConfig: MappingConfig, csvFormat: CSVFormat, financePmDataHolder: FinancePmDataHolder) =
        CategoryMapper(mappingConfig.categories, csvFormat, financePmDataHolder)

    @Bean
    fun transactionMapper(accountMapper: AccountMapper, categoryMapper: CategoryMapper, financePmDataHolder: FinancePmDataHolder) =
        TransactionMapper(categoryMapper, accountMapper, financePmDataHolder)

    @Bean
    fun transactionProcessor(financePmDataHolder: FinancePmDataHolder,
                             transactionMapper: TransactionMapper,
                             categoryMapper: CategoryMapper
    ) =
        TransactionProcessor(financePmDataHolder, transactionMapper, categoryMapper)

    @Bean
    fun transferProcessor(financePmDataHolder: FinancePmDataHolder,
                          transactionMapper: TransactionMapper,
                          transactionProcessor: TransactionProcessor
    ) =
        TransferProcessor(financePmDataHolder, transactionMapper, transactionProcessor)
}

open class MappingConfig (
    var accounts: String = "",
    var categories: String = ""
)

open class OutputFileConfig (
    var name: String = "financePM_{date}_enriched.data",
    var datePattern: String = "dd-MM-yyyy"
)