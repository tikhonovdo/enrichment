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
import ru.tikhonovdo.enrichment.batch.matching.config.yandex.YandexMatchingJobConfig

@Configuration
@Import(BaseMatchingJobConfig::class,
    RefundFeatureConfig::class,
    TransferManualMatchingConfig::class,
    AlfaMatchingJobConfig::class,
    YandexMatchingJobConfig::class,
    TinkoffMatchingJobConfig::class)
class MatchingJobConfig(jobRepository: JobRepository): AbstractJobConfig(jobRepository) {

    @Bean
    fun matchingJob(
        matchingFlow: Flow,
        postMatchingFlow: Flow,
        validatableRowCountStep: Step
    ): Job {
        return job("matchingJob")
            .start(matchingFlow)
            .next(postMatchingFlow)
            .next(validatableRowCountStep)
            .end()
            .build()
    }

    @Bean
    fun matchingFlow(
        tinkoffMatchingFlow: Flow,
        alfaMatchingFlow: Flow,
        yandexMatchingFlow: Flow,
        cleanUnmatchedTransactionsStep: Step,
        transferMatchingStep: Step,
        transferComplementStep: Step,
        transferManualMatchingStep: Step,
        exportMatchingTransactionsStep: Step,
        exportMatchedTransfersStep: Step,
    ): Flow {
        return CustomFlowBuilder("matchingFlow")
            .addStep(cleanUnmatchedTransactionsStep)
            .addStep(flowStep(tinkoffMatchingFlow))
            .addStep(flowStep(alfaMatchingFlow))
            .addStep(flowStep(yandexMatchingFlow))
            .addStep(transferComplementStep)
            .addStep(transferMatchingStep)
            .addStep(transferManualMatchingStep)
            .addStep(exportMatchingTransactionsStep)
            .addStep(exportMatchedTransfersStep)
            .build()
    }

    @Bean
    fun postMatchingFlow(
        matchWithMasterTransactionsStep: Step,
        syncWithMatchedTransactionsStep: Step,
        searchRefundStep: Step,
        applyRefundStep: Step,
    ): Flow {
        return CustomFlowBuilder("postMatchingFlow")
            .addStep(searchRefundStep)
            .addStep(matchWithMasterTransactionsStep)
            .addStep(syncWithMatchedTransactionsStep)
            .addStep(applyRefundStep)
            .build()
    }

}