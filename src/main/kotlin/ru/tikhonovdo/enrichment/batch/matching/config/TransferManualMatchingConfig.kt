package ru.tikhonovdo.enrichment.batch.matching.config

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.transfer.manual.TransferManualMatchingInfo
import ru.tikhonovdo.enrichment.batch.matching.transfer.manual.TransferManualMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transfer.manual.TransferManualMatchingStepReader
import ru.tikhonovdo.enrichment.domain.enitity.TransferMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import ru.tikhonovdo.enrichment.repository.matching.TransferMatchingRepository
import javax.sql.DataSource

@Configuration
class TransferManualMatchingConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val transactionManager: PlatformTransactionManager,
    private val transferMatchingRepository: TransferMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository
): AbstractJobConfig(jobRepository) {

    @Bean
    fun transferManualMatchingStep(
        transferManualMatchingStepReader: ItemReader<TransferManualMatchingInfo>,
        transferManualMatchingStepProcessor: ItemProcessor<TransferManualMatchingInfo, Collection<TransferMatching>>
    ): Step {
        return step("transferManualMatchingStep")
            .chunk<TransferManualMatchingInfo, Collection<TransferMatching>>(10, transactionManager)
            .reader(transferManualMatchingStepReader)
            .processor(transferManualMatchingStepProcessor)
            .writer { transferMatchingRepository.insertBatch(it.items.flatten()) }
            .build()
    }

    @Bean
    fun transferManualMatchingStepReader(): ItemReader<TransferManualMatchingInfo> =
        TransferManualMatchingStepReader(dataSource)

    @Bean
    fun transferManualMatchingStepProcessor(): ItemProcessor<TransferManualMatchingInfo, Collection<TransferMatching>> =
        TransferManualMatchingStepProcessor(transferMatchingRepository, transactionMatchingRepository)

}