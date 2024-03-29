package ru.tikhonovdo.enrichment.batch

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import ru.tikhonovdo.enrichment.batch.common.AbstractJobConfig
import ru.tikhonovdo.enrichment.batch.common.CustomFlowBuilder
import ru.tikhonovdo.enrichment.batch.matching.account.TinkoffDirectAccountMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.account.tinkoff.TinkoffImplicitAccountMatchingStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.account.tinkoff.TinkoffImplicitAccountMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.category.TinkoffCategoryMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.currency.TinkoffCurrencyMatchingStepReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff.TinkoffRecordReader
import ru.tikhonovdo.enrichment.batch.matching.transaction.tinkoff.TinkoffTransactionStepProcessor
import ru.tikhonovdo.enrichment.batch.matching.transfer.*
import ru.tikhonovdo.enrichment.batch.matching.transfer.tinkoff.*
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.*
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.repository.financepm.AccountRepository
import ru.tikhonovdo.enrichment.repository.matching.*
import javax.sql.DataSource

@Configuration
@Import(BaseMatchingJobConfig::class)
class TinkoffMatchingJobConfig(
    jobRepository: JobRepository,
    private val dataSource: DataSource,
    private val transactionManager: PlatformTransactionManager,
    private val accountRepository: AccountRepository,
    private val draftTransactionRepository: DraftTransactionRepository,
    private val categoryMatchingRepository: CategoryMatchingRepository,
    private val accountMatchingRepository: AccountMatchingRepository,
    private val tinkoffAccountMatchingRepository: TinkoffAccountMatchingRepository,
    private val currencyMatchingRepository: CurrencyMatchingRepository,
    private val transactionMatchingRepository: TransactionMatchingRepository,
    private val transferMatchingRepository: TransferMatchingRepository,
): AbstractJobConfig(jobRepository) {

    @Bean
    fun tinkoffMatchingFlow(
        tinkoffCategoryMatchingStep: Step,
        tinkoffCurrencyMatchingStep: Step,
        tinkoffAccountMatchingFlow: Flow,
        tinkoffTransactionMatchingStep: Step,
        tinkoffTransferMatchingFlow: Flow
    ): Flow = CustomFlowBuilder("tinkoffMatchingFlow")
        .addStep(tinkoffCategoryMatchingStep)
        .addStep(tinkoffCurrencyMatchingStep)
        .addStep(flowStep(tinkoffAccountMatchingFlow))
        .addStep(tinkoffTransactionMatchingStep)
        .addStep(flowStep(tinkoffTransferMatchingFlow))
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
    fun tinkoffCategoryMatchingStepReader(): ItemReader<CategoryMatching> =
        TinkoffCategoryMatchingStepReader(dataSource)
    
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
    fun tinkoffCurrencyMatchingStepReader(): ItemReader<CurrencyMatching> =
        TinkoffCurrencyMatchingStepReader(dataSource)

    @Bean
    fun tinkoffAccountMatchingFlow(
        tinkoffAccountMatchingStep: Step,
        tinkoffImplicitAccountMatchingStep: Step
    ): Flow {
        return CustomFlowBuilder("tinkoffAccountMatchingFlow")
            .addStep(tinkoffAccountMatchingStep)
            .addStep(tinkoffImplicitAccountMatchingStep)
            .build()
    }

    @Bean
    fun tinkoffAccountMatchingStep(
        tinkoffAccountMatchingStepReader: ItemReader<AccountMatching>,
        accountMatchingStepProcessor: ItemProcessor<AccountMatching, AccountMatching>
    ): Step {
        return step("tinkoffAccountMatchingStep")
            .chunk<AccountMatching, AccountMatching>(10, transactionManager)
            .reader(tinkoffAccountMatchingStepReader)
            .processor(accountMatchingStepProcessor)
            .writer { accountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffAccountMatchingStepReader(): ItemReader<AccountMatching> =
        TinkoffDirectAccountMatchingStepReader(dataSource)

    @Bean
    fun tinkoffImplicitAccountMatchingStep(
        tinkoffImplicitAccountMatchingStepReader: ItemReader<AccountMatching.Tinkoff>,
        tinkoffImplicitAccountMatchingStepProcessor: ItemProcessor<AccountMatching.Tinkoff, AccountMatching.Tinkoff>,
    ): Step {
        return step("tinkoffImplicitAccountMatchingStep")
            .chunk<AccountMatching.Tinkoff, AccountMatching.Tinkoff>(10, transactionManager)
            .reader(tinkoffImplicitAccountMatchingStepReader)
            .processor(tinkoffImplicitAccountMatchingStepProcessor)
            .writer { tinkoffAccountMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffImplicitAccountMatchingStepReader(): ItemReader<AccountMatching.Tinkoff> =
        TinkoffImplicitAccountMatchingStepReader(dataSource)

    @Bean
    fun tinkoffImplicitAccountMatchingStepProcessor(): ItemProcessor<AccountMatching.Tinkoff, AccountMatching.Tinkoff> =
        TinkoffImplicitAccountMatchingStepProcessor(tinkoffAccountMatchingRepository)

    @Bean
    fun tinkoffTransactionMatchingStep(
        tinkoffTransactionMatchingStepReader: ItemReader<TinkoffRecord>,
        tinkoffTransactionMatchingStepProcessor: ItemProcessor<TinkoffRecord, TransactionMatching>
    ): Step {
        return step("tinkoffTransactionMatchingStep")
            .chunk<TinkoffRecord, TransactionMatching>(10, transactionManager)
            .reader(tinkoffTransactionMatchingStepReader)
            .listener(tinkoffTransactionMatchingStepProcessor)
            .processor(tinkoffTransactionMatchingStepProcessor)
            .writer { transactionMatchingRepository.insertBatch(it.items) }
            .build()
    }

    @Bean
    fun tinkoffTransactionMatchingStepReader(): ItemReader<TinkoffRecord> =
        TinkoffRecordReader(dataSource)

    @Bean
    fun tinkoffTransactionMatchingStepProcessor(): ItemProcessor<TinkoffRecord, TransactionMatching> =
        TinkoffTransactionStepProcessor(
            draftTransactionRepository,
            transactionMatchingRepository,
            categoryMatchingRepository,
            accountMatchingRepository,
            tinkoffAccountMatchingRepository
        )

    @Bean
    fun tinkoffTransferMatchingFlow(
        tinkoffAccountsTransferMatchingStep: Step,
        tinkoffCashTransferMatchingStep: Step
    ): Flow {
        return CustomFlowBuilder("tinkoffTransferMatchingFlow")
            .addStep(tinkoffAccountsTransferMatchingStep)
            .addStep(tinkoffCashTransferMatchingStep)
            .build()
    }

    @Bean
    fun tinkoffAccountsTransferMatchingStep(
        tinkoffAccountsTransferMatchingStepReader: ItemReader<TransferMatching>,
        tinkoffAccountsTransferMatchingStepProcessor: ItemProcessor<TransferMatching, TransferMatching>,
        tinkoffAccountsTransferMatchingStepWriter: ItemWriter<TransferMatching>
    ): Step {
        return step("tinkoffTransferMatchingStep")
            .chunk<TransferMatching, TransferMatching>(10, transactionManager)
            .reader(tinkoffAccountsTransferMatchingStepReader)
            .processor(tinkoffAccountsTransferMatchingStepProcessor)
            .writer(tinkoffAccountsTransferMatchingStepWriter)
            .build()
    }

    @Bean
    fun tinkoffAccountsTransferMatchingStepReader(): ItemReader<TransferMatching> =
        TinkoffAccountsTransferMatchingStepReader(dataSource)

    @Bean
    fun tinkoffAccountsTransferMatchingStepProcessor(): ItemProcessor<TransferMatching, TransferMatching> =
        TinkoffAccountsTransferMatchingStepProcessor(transferMatchingRepository)

    @Bean
    fun tinkoffAccountsTransferMatchingStepWriter(): ItemWriter<TransferMatching> =
        TinkoffAccountsTransferMatchingStepWriter(transactionMatchingRepository, transferMatchingRepository)

    @Bean
    fun tinkoffCashTransferMatchingStep(
        tinkoffCashTransferMatchingStepReader: ItemReader<TransactionMatching>,
        tinkoffCashTransferMatchingStepWriter: ItemWriter<TransactionMatching>,
        tinkoffCashTransferMatchingStepProcessor: ItemProcessor<TransactionMatching, TransactionMatching>
    ): Step {
        return step("tinkoffCashTransferMatchingStep")
            .chunk<TransactionMatching, TransactionMatching>(10, transactionManager)
            .reader(tinkoffCashTransferMatchingStepReader)
            .processor(tinkoffCashTransferMatchingStepProcessor)
            .writer(tinkoffCashTransferMatchingStepWriter)
            .build()
    }

    @Bean
    fun tinkoffCashTransferMatchingStepReader(): ItemReader<TransactionMatching> =
        TinkoffCashTransferMatchingStepReader(dataSource)

    @Bean
    fun tinkoffCashTransferMatchingStepProcessor(): ItemProcessor<TransactionMatching, TransactionMatching> =
        TinkoffCashTransferMatchingStepProcessor(transactionMatchingRepository)

    @Bean
    fun tinkoffCashTransferMatchingStepWriter(): ItemWriter<TransactionMatching> =
        TinkoffCashTransferMatchingStepWriter(
            accountRepository, transactionMatchingRepository, transferMatchingRepository
        )
}