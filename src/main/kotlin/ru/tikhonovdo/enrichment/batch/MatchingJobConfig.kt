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
                    matchedTransfersExportStep: Step
    ): Job {
        return job("matchingJob")
            .start(tinkoffMatchingFlow)
            .next(matchedTransactionsExportStep)
            .next(matchedTransfersExportStep)
            .end()
            .build()
    }

}