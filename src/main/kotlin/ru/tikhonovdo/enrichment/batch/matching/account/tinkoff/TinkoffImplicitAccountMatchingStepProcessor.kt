package ru.tikhonovdo.enrichment.batch.matching.account.tinkoff

import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.matching.TinkoffAccountMatchingRepository

class TinkoffImplicitAccountMatchingStepProcessor(
    private val tinkoffAccountMatchingRepository: TinkoffAccountMatchingRepository
): ItemProcessor<AccountMatching.Tinkoff, AccountMatching.Tinkoff>, StepExecutionListener {

    private var accountMatchingCandidates = listOf<AccountMatching.Tinkoff>()

    override fun beforeStep(stepExecution: StepExecution) {
        accountMatchingCandidates = tinkoffAccountMatchingRepository.findAll()
    }

    override fun process(item: AccountMatching.Tinkoff): AccountMatching.Tinkoff? {
        val probe = AccountMatching.Tinkoff(
            bankCurrencyCode = item.bankCurrencyCode,
            pattern = item.pattern
        )

        return if (accountMatchingCandidates.contains(probe)) {
            null
        } else {
            item
        }
    }
}