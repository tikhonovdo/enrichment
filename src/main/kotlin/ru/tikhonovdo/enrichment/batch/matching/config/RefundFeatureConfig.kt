package ru.tikhonovdo.enrichment.batch.matching.config

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.matching.transfer.refund.*
import ru.tikhonovdo.enrichment.domain.enitity.TransactionMatching
import ru.tikhonovdo.enrichment.repository.financepm.TransactionRepository
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository
import javax.sql.DataSource

@Configuration
class RefundFeatureConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val transactionManager: PlatformTransactionManager,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val transactionRepository: TransactionRepository
): AbstractJobConfig(jobRepository) {

    @Bean
    fun searchRefundStep(
        searchRefundStepReader: ItemReader<TransactionMatching>,
        searchRefundStepProcessor: ItemProcessor<TransactionMatching, TransactionMatching>
    ): Step {
        return step("searchRefundStep")
            .chunk<TransactionMatching, TransactionMatching>(10, transactionManager)
            .reader(searchRefundStepReader)
            .processor(searchRefundStepProcessor)
            .writer { transactionMatchingRepository.batchUpdateRefundForId(it.items) }
            .build()
    }

    @Bean
    fun searchRefundStepReader(): ItemReader<TransactionMatching> =
        SearchRefundStepReader(dataSource)

    @Bean
    fun searchRefundStepProcessor(): ItemProcessor<TransactionMatching, TransactionMatching> =
        SearchRefundStepProcessor(transactionMatchingRepository)

    @Bean
    fun applyRefundStep(
        applyRefundStepReader: ItemReader<ApplyRefundInfo>,
        applyRefundStepProcessor: ItemProcessor<ApplyRefundInfo, ApplyRefundInfo?>
    ): Step {
        return step("applyRefundStep")
            .chunk<ApplyRefundInfo, ApplyRefundInfo?>(10, transactionManager)
            .reader(applyRefundStepReader)
            .processor(applyRefundStepProcessor)
            .build()
    }

    @Bean
    fun applyRefundStepReader(): ItemReader<ApplyRefundInfo> =
        ApplyRefundStepReader(dataSource)

    @Bean
    fun applyRefundStepProcessor(
        @Value("\${refund.income-category-id}") refundIncomeCategoryId: Long
    ): ItemProcessor<ApplyRefundInfo, ApplyRefundInfo?> =
        ApplyRefundStepProcessor(refundIncomeCategoryId, transactionRepository, transactionMatchingRepository)

}