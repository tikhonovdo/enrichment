package ru.tikhonovdo.enrichment.batch.matching.account

import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.item.ItemProcessor
import ru.tikhonovdo.enrichment.domain.enitity.AccountMatching
import ru.tikhonovdo.enrichment.repository.matching.AccountMatchingRepository

class AccountMatchingStepProcessor(
    private val accountMatchingRepository: AccountMatchingRepository
): ItemProcessor<AccountMatching, AccountMatching>, StepExecutionListener {

    private var accountMatchingCandidates = listOf<AccountMatching>()

    override fun beforeStep(stepExecution: StepExecution) {
        accountMatchingCandidates = accountMatchingRepository.findAll()
    }

    override fun process(item: AccountMatching): AccountMatching? {
        val probe = AccountMatching(
            bankId = item.bankId,
            bankAccountCode = item.bankAccountCode
        )

        return if (accountMatchingCandidates.contains(probe)) {
            item
        } else {
            null
        }
    }
}