package ru.tikhonovdo.enrichment.batch.matching.config

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.common.CustomFlowBuilder
import ru.tikhonovdo.enrichment.batch.matching.config.alfa.AlfaMatchingJobConfig
import ru.tikhonovdo.enrichment.batch.matching.config.tinkoff.TinkoffMatchingJobConfig

@Configuration
@Import(BaseMatchingJobConfig::class, AlfaMatchingJobConfig::class, TinkoffMatchingJobConfig::class)
class MatchingJobConfig(jobRepository: JobRepository): AbstractJobConfig(jobRepository) {

    @Bean
    fun matchingJob(tinkoffMatchingFlow: Flow,
                    alfaMatchingFlow: Flow,
                    transferMatchingFlow: Flow,
                    matchedTransactionsExportStep: Step,
                    linkWithMatchedTransactionsStep: Step,
                    actualizeMatchedTransactionsStep: Step,
                    matchedTransfersExportStep: Step,
                    cleanUnmatchedTransactionsStep: Step
    ): Job {
        return job("matchingJob")
            .flow(cleanUnmatchedTransactionsStep)
            .next(tinkoffMatchingFlow)
            .next(alfaMatchingFlow)
            .next(transferMatchingFlow)
            .next(matchedTransactionsExportStep)
            .next(linkWithMatchedTransactionsStep)
            .next(actualizeMatchedTransactionsStep)
            .next(matchedTransfersExportStep)
            .end()
            .build()
    }

    @Bean
    fun transferMatchingFlow(
        transferMatchingStep: Step,
        tinkoffCashTransferMatchingStep: Step,
        alfaCashTransferMatchingStep: Step
    ): Flow {
        return CustomFlowBuilder("transferMatchingFlow")
            .addStep(transferMatchingStep)
            .addStep(tinkoffCashTransferMatchingStep)
            .addStep(alfaCashTransferMatchingStep)
            .build()
    }

}