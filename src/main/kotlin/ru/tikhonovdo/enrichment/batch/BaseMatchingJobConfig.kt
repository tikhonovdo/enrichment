package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.account.AccountMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.category.CategoryMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.currency.CurrencyMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.MatchedTransactionsExportStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.MatchedTransactionsExportStepReader
import ru.tikhonovdo.enrichment.batch.matching.transfer.base.TransferMatchingExportTasklet
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CurrencyMatchingRepository
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
    private val transactionRepository: TransactionRepository
): AbstractJobConfig(jobRepository) {

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
    fun matchedTransactionsExportStep(
        matchedTransactionsExportStepReader: ItemReader<TransactionMatching>,
        matchedTransactionsExportStepProcessor: ItemProcessor<TransactionMatching, Transaction>,
    ): Step {
        return step("matchedTransactionsExportStep")
            .chunk<TransactionMatching, Transaction>(10, transactionManager)
            .reader(matchedTransactionsExportStepReader)
            .processor(matchedTransactionsExportStepProcessor)
            .writer { transactionRepository.saveBatch(it.items) }
            .build()
    }

    @Bean
    fun matchedTransactionsExportStepReader(): ItemReader<TransactionMatching> =
        MatchedTransactionsExportStepReader(dataSource)

    @Bean
    fun matchedTransactionsExportStepProcessor(): ItemProcessor<TransactionMatching, Transaction> =
        MatchedTransactionsExportStepProcessor(transactionRepository)


    @Bean
    fun matchedTransfersExportStep(): Step {
        return step("matchedTransfersExportStep")
            .tasklet(TransferMatchingExportTasklet(jdbcTemplate), transactionManager)
            .build()
    }

}