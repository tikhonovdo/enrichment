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
    fun matchingJob(matchingFlow: Flow): Job {
        return job("matchingJob")
            .start(matchingFlow)
            .end()
            .build()
    }

    @Bean
    fun matchingFlow(
        tinkoffMatchingFlow: Flow,
        alfaMatchingFlow: Flow,
        transferMatchingFlow: Flow,
        matchedTransactionsExportStep: Step,
        linkWithMatchedTransactionsStep: Step,
        actualizeMatchedTransactionsStep: Step,
        matchedTransfersExportStep: Step,
        cleanUnmatchedTransactionsStep: Step
    ): Flow {
        return CustomFlowBuilder("matchingFlow")
            .addStep(cleanUnmatchedTransactionsStep)
            .addStep(flowStep(tinkoffMatchingFlow))
            .addStep(flowStep(alfaMatchingFlow))
            .addStep(flowStep(transferMatchingFlow))
            .addStep(matchedTransactionsExportStep)
            .addStep(linkWithMatchedTransactionsStep)
            .addStep(actualizeMatchedTransactionsStep)
            .addStep(matchedTransfersExportStep)
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