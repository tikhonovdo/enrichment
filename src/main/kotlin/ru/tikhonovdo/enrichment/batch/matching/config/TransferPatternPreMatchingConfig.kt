package ru.tikhonovdo.enrichment.batch.matching.config

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.transfer.pattern.PatternTransferMatchingInfo
import ru.tikhonovdo.enrichment.batch.matching.transfer.pattern.TransferPatternPreMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transfer.pattern.TransferPatternPreMatchingStepReader
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import javax.sql.DataSource

@Configuration
class TransferPatternPreMatchingConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val transactionManager: PlatformTransactionManager,
    private val transactionMatchingRepository: TransactionMatchingRepository
): AbstractJobConfig(jobRepository) {

    @Bean
    fun transferPatternPreMatchingStep(
        transferPatternPreMatchingStepReader: ItemReader<PatternTransferMatchingInfo>,
        transferPatternPreMatchingStepProcessor: ItemProcessor<PatternTransferMatchingInfo, List<TransactionMatching>>
    ): Step {
        return step("transferPatternPreMatchingStep")
            .chunk<PatternTransferMatchingInfo, List<TransactionMatching>>(10, transactionManager)
            .reader(transferPatternPreMatchingStepReader)
            .processor(transferPatternPreMatchingStepProcessor)
            .writer { transactionMatchingRepository.insertBatch(it.items.flatten()) }
            .build()
    }

    @Bean
    fun transferPatternPreMatchingStepReader(): ItemReader<PatternTransferMatchingInfo> =
        TransferPatternPreMatchingStepReader(dataSource)

    @Bean
    fun transferPatternPreMatchingStepProcessor(): ItemProcessor<PatternTransferMatchingInfo, List<TransactionMatching>> =
        TransferPatternPreMatchingStepProcessor(transactionMatchingRepository)

}