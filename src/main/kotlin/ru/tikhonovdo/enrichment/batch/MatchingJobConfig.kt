package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig

@Configuration
@Import(TinkoffMatchingJobConfig::class, BaseMatchingJobConfig::class)
 class MatchingJobConfig(jobRepository: JobRepository): AbstractJobConfig(jobRepository) {

    @Bean
    fun matchingJob(tinkoffMatchingFlow: Flow,
                    matchedTransactionsExportStep: Step,
                    linkWithMatchedTransactionsStep: Step,
                    actualizeMatchedTransactionsStep: Step,
                    matchedTransfersExportStep: Step,
                    cleanUnmatchedTransactionsStep: Step
    ): Job {
        return job("matchingJob")
            .flow(cleanUnmatchedTransactionsStep)
            .next(tinkoffMatchingFlow)
            .next(matchedTransactionsExportStep)
            .next(linkWithMatchedTransactionsStep)
            .next(actualizeMatchedTransactionsStep)
            .next(matchedTransfersExportStep)
            .end()
            .build()
    }

}