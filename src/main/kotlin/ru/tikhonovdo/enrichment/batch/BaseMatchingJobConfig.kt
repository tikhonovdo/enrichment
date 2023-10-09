package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.account.AccountMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.category.CategoryMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.currency.CurrencyMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.ActualizeMatchedTransactionsTasklet
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.LinkWithMatchedTransactionsTasklet
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.MatchedTransactionsExportTasklet
import ru.tikhonovdo.enrichment.batch.matching.transaction.base.ValidationNeededRowCounter
import ru.tikhonovdo.enrichment.batch.matching.transfer.base.TransferMatchingExportTasklet
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CategoryMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.CurrencyMatchingRepository

@Configuration
class BaseMatchingJobConfig(
    jobRepository: JobRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val transactionManager: PlatformTransactionManager,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val currencyMatchingRepository: CurrencyMatchingRepository
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
    fun matchedTransactionsExportStep(): Step {
        return step("matchedTransactionsExportStep")
            .tasklet(MatchedTransactionsExportTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun linkWithMatchedTransactionsStep(): Step {
        return step("linkWithMatchedTransactionsStep")
            .tasklet(LinkWithMatchedTransactionsTasklet(jdbcTemplate), transactionManager)
            .build()
    }

    @Bean
    fun actualizeMatchedTransactionsStep(): Step {
        return step("actualizeMatchedTransactionsStep")
            .tasklet(ActualizeMatchedTransactionsTasklet(jdbcTemplate), transactionManager)
            .listener(ValidationNeededRowCounter(jdbcTemplate))
            .build()
    }


    @Bean
    fun matchedTransfersExportStep(): Step {
        return step("matchedTransfersExportStep")
            .tasklet(TransferMatchingExportTasklet(jdbcTemplate), transactionManager)
            .build()
    }

}