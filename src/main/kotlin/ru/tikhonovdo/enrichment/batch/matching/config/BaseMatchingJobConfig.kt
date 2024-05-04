package ru.tikhonovdo.enrichment.batch.matching.config

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.account.AccountMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.category.CategoryMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.currency.CurrencyMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transaction.*
import ru.tikhonovdo.enrichment.batch.matching.transfer.ExportMatchedTransfersTasklet
import ru.tikhonovdo.enrichment.batch.matching.transfer.TransferMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transfer.TransferMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.transfer.TransferMatchingStepWriter
import ru.tikhonovdo.enrichment.batch.matching.transfer.complement.TransferComplementInfo
import ru.tikhonovdo.enrichment.batch.matching.transfer.complement.TransferComplementStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transfer.complement.TransferComplementStepReader
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.repository.matching.*
import javax.sql.DataSource

@Configuration
class BaseMatchingJobConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate,
    private val transactionManager: PlatformTransactionManager,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val currencyMatchingRepository: CurrencyMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val transferMatchingRepository: TransferMatchingRepository
): AbstractJobConfig(jobRepository) {

    @Bean
    fun transferMatchingStep(
        transferMatchingStepReader: ItemReader<TransferMatching>,
        transferMatchingStepProcessor: ItemProcessor<TransferMatching, TransferMatching>,
        transferMatchingStepWriter: ItemWriter<TransferMatching>
    ): Step {
        return step("transferMatchingStep")
            .chunk<TransferMatching, TransferMatching>(10, transactionManager)
            .reader(transferMatchingStepReader)
            .processor(transferMatchingStepProcessor)
            .writer(transferMatchingStepWriter)
            .build()
    }

    @Bean
    fun transferMatchingStepReader(): ItemReader<TransferMatching> =
        TransferMatchingStepReader(dataSource)

    @Bean
    fun transferMatchingStepProcessor(): ItemProcessor<TransferMatching, TransferMatching> =
        TransferMatchingStepProcessor(transferMatchingRepository)

    @Bean
    fun transferMatchingStepWriter(): ItemWriter<TransferMatching> =
        TransferMatchingStepWriter(transactionMatchingRepository, transferMatchingRepository)

    @Bean
    fun categoryMatchingStepProcessor(): ItemProcessor<CategoryMatching, CategoryMatching> =
        CategoryMatchingStepProcessor(categoryMatchingRepository)

    @Bean
    fun currencyMatchingStepProcessor(): ItemProcessor<CurrencyMatching, CurrencyMatching> =
        CurrencyMatchingStepProcessor(currencyMatchingRepository)

    @Bean
    fun accountMatchingStepProcessor(): ItemProcessor<AccountMatching, AccountMatching> =
        AccountMatchingStepProcessor(accountMatchingRepository)

    @Bean
    fun transferComplementStep(
        transferComplementStepReader: ItemReader<TransferComplementInfo>,
        transferComplementStepProcessor: ItemProcessor<TransferComplementInfo, Collection<TransactionMatching>>
    ): Step {
        return step("transferComplementStep")
            .chunk<TransferComplementInfo, Collection<TransactionMatching>>(10, transactionManager)
            .reader(transferComplementStepReader)
            .processor(transferComplementStepProcessor)
            .writer { transactionMatchingRepository.insertBatch(it.items.flatten()) }
            .build()
    }

    @Bean
    fun transferComplementStepReader(): ItemReader<TransferComplementInfo> =
        TransferComplementStepReader(dataSource)

    @Bean
    fun transferComplementStepProcessor(): ItemProcessor<TransferComplementInfo, Collection<TransactionMatching>> =
        TransferComplementStepProcessor(transactionMatchingRepository)

    @Bean
    fun exportMatchingTransactionsStep(): Step {
        return step("exportMatchingTransactionsStep")
            .tasklet(ExportMatchingTransactionsTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun matchWithMasterTransactionsStep(): Step {
        return step("matchWithMasterTransactionsStep")
            .tasklet(MatchWithMasterTransactionsTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun syncWithMatchedTransactionsStep(): Step {
        return step("syncWithMatchedTransactionsStep")
            .tasklet(SyncWithNotFinalizedMatchedTransactionsTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun validatableRowCountStep(): Step {
        return step("validatableRowCountStep")
            .tasklet(ValidatableRowCounter(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun exportMatchedTransfersStep(): Step {
        return step("exportMatchedTransfersStep")
            .tasklet(ExportMatchedTransfersTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun cleanUnmatchedTransactionsStep(): Step {
        return step("cleanUnmatchedTransactionsStep")
            .tasklet(CleanUnmatchedTransactionsTasklet(transactionMatchingRepository), transactionManager)
            .build()
    }

}