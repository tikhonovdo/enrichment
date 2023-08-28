package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.FlowStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.CustomFlowBuilder
import ru.tikhonovdo.enrichment.batch.matching.*
import ru.tikhonovdo.enrichment.batch.matching.account.AccountMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.account.TinkoffDirectAccountMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.account.TinkoffImplicitAccountMatchingStepReader
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.*
import javax.sql.DataSource

@Configuration
 class MatchingJobConfig(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val draftTransactionRepository: DraftTransactionRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val currencyMatchingRepository: CurrencyMatchingRepository,
) {

    @Bean
    fun matchingJob(matchingFlow: Flow): Job {
        return job("matchingJob")
            .start(matchingFlow)
            .end()
            .build()
    }

    @Bean
    fun matchingFlow(
        tinkoffCategoryMatchingStep: Step,
        tinkoffCurrencyMatchingStep: Step,
        tinkoffAccountMatchingFlow: Flow,
//        transactionMatchingStep: Step,
//        transferMatchingStep: Step
    ): Flow = CustomFlowBuilder("matchingFlow")
            .addStep(tinkoffCategoryMatchingStep)
            .addStep(tinkoffCurrencyMatchingStep)
            .addStep(FlowStepBuilder(step("tinkoffAccountMatchingStep"))
                .flow(tinkoffAccountMatchingFlow)
                .build())
//            .addStep(transactionMatchingStep)
//            .addStep(transferMatchingStep)
            .build()

    @Bean
    fun tinkoffCategoryMatchingStep(
        tinkoffCategoryMatchingStepReader: ItemReader<CategoryMatching>,
        categoryMatchingStepProcessor: ItemProcessor<CategoryMatching, CategoryMatching>
    ): Step {
        return step("tinkoffCategoryMatchingStep")
            .chunk<CategoryMatching, CategoryMatching>(10, transactionManager)
            .reader(tinkoffCategoryMatchingStepReader)
            .processor(categoryMatchingStepProcessor)
            .writer { categoryMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffCategoryMatchingStepReader(): ItemReader<CategoryMatching> {
        return TinkoffCategoryMatchingStepReader(dataSource)
    }

    @Bean
    fun categoryMatchingStepProcessor(): ItemProcessor<CategoryMatching, CategoryMatching> =
        CategoryMatchingStepProcessor(categoryMatchingRepository)

    @Bean
    fun tinkoffCurrencyMatchingStep(
        tinkoffCurrencyMatchingStepReader: ItemReader<CurrencyMatching>,
        currencyMatchingStepProcessor: ItemProcessor<CurrencyMatching, CurrencyMatching>
    ): Step {
        return step("tinkoffCurrencyMatchingStep")
            .chunk<CurrencyMatching, CurrencyMatching>(10, transactionManager)
            .reader(tinkoffCurrencyMatchingStepReader)
            .processor(currencyMatchingStepProcessor)
            .writer { currencyMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffCurrencyMatchingStepReader(): ItemReader<CurrencyMatching> {
        return TinkoffCurrencyMatchingStepReader(dataSource)
    }

    @Bean
    fun currencyMatchingStepProcessor(): ItemProcessor<CurrencyMatching, CurrencyMatching> =
        CurrencyMatchingStepProcessor(currencyMatchingRepository)

    @Bean
    fun tinkoffDirectAccountMatchingStep(
        tinkoffDirectAccountMatchingStepReader: ItemReader<AccountMatching>,
        accountMatchingStepProcessor: ItemProcessor<AccountMatching, AccountMatching>
    ): Step {
        return step("tinkoffDirectAccountMatchingStep")
            .chunk<AccountMatching, AccountMatching>(10, transactionManager)
            .reader(tinkoffDirectAccountMatchingStepReader)
            .processor(accountMatchingStepProcessor)
            .writer { accountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffImplicitAccountMatchingStep(
        tinkoffImplicitAccountMatchingStepReader: ItemReader<AccountMatching>,
        accountMatchingStepProcessor: ItemProcessor<AccountMatching, AccountMatching>
    ): Step {
        return step("tinkoffImplicitAccountMatchingStep")
            .chunk<AccountMatching, AccountMatching>(10, transactionManager)
            .reader(tinkoffImplicitAccountMatchingStepReader)
            .processor(accountMatchingStepProcessor)
            .writer { accountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffAccountMatchingFlow(
        tinkoffDirectAccountMatchingStep: Step,
        tinkoffImplicitAccountMatchingStep: Step
    ): Flow {
        return CustomFlowBuilder("tinkoffAccountMatchingFlow")
            .addStep(tinkoffDirectAccountMatchingStep)
            .addStep(tinkoffImplicitAccountMatchingStep)
            .build()
    }


    @Bean
    fun tinkoffDirectAccountMatchingStepReader(): ItemReader<AccountMatching> {
        return TinkoffDirectAccountMatchingStepReader(dataSource)
    }

    @Bean
    fun tinkoffImplicitAccountMatchingStepReader(): ItemReader<AccountMatching> {
        return TinkoffImplicitAccountMatchingStepReader(dataSource)
    }

    @Bean
    fun accountMatchingStepProcessor(): ItemProcessor<AccountMatching, AccountMatching> =
        AccountMatchingStepProcessor(accountMatchingRepository)

//
//    @Bean
//    fun transactionMatchingStep(): Step {
//        return step("transactionMatchingStep")
//            .chunk<>(10, transactionManager)
//            .reader()
//            .processor()
//            .writer()
//            .build()
//    }
//
//    @Bean
//    fun transferMatchingStep(): Step {
//        return step("transferMatchingStep")
//            .chunk<>(10, transactionManager)
//            .reader()
//            .processor()
//            .writer()
//            .build()
//    }
//
    private fun step(name: String): StepBuilder = StepBuilder(name, jobRepository).allowStartIfComplete(true)
    private fun job(name: String): JobBuilder = JobBuilder(name, jobRepository)

}