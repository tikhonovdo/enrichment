package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionManager
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.CurrencyMatching

@Configuration
@EnableBatchProcessing
@Import(DataSourceAutoConfiguration::class)
class MatchingJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
){

    @Bean
    fun matchingJob(
//        categoryMatchingStep: Step,
//        currencyMatchingStep: Step,
//        accountMatchingStep: Step,
//        transactionMatchingStep: Step,
//        transferMatchingStep: Step
    ): Job {
        return job("matchingJob")
            .start(step("test tasklet")
                .tasklet({ _, _ ->
                    println("matchingJob called")
                    RepeatStatus.FINISHED
                }, transactionManager)
                .build())
//            .start(categoryMatchingStep)
//            .next(currencyMatchingStep)
//            .next(accountMatchingStep)
//            .next(transactionMatchingStep)
//            .next(transferMatchingStep)
            .build()
    }
//
//    @Bean
//    fun categoryMatchingStep(): Step {
//        return step("categoryMatchingStep")
//            .chunk<CategoryMatching, CategoryMatching>(10, transactionManager)
//            .reader()
//            .processor()
//            .writer()
//            .build()
//    }
//
//    @Bean
//    fun currencyMatchingStep(): Step {
//        return step("currencyMatchingStep")
//            .chunk<CurrencyMatching, CurrencyMatching>(10, transactionManager)
//            .reader()
//            .processor()
//            .writer()
//            .build()
//    }
//
//    @Bean
//    fun accountMatchingStep(): Step {
//        return step("accountMatchingStep")
//            .chunk<>(10, transactionManager)
//            .reader()
//            .processor()
//            .writer()
//            .build()
//    }
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
    private fun step(name: String): StepBuilder = StepBuilder(name, jobRepository)
    private fun job(name: String): JobBuilder = JobBuilder(name, jobRepository)

}