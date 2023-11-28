package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

class CleanUnmatchedTransactionsTasklet(private val transactionMatchingRepository: TransactionMatchingRepository): Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val ids = transactionMatchingRepository.getUnmatchedTransactionIds()
        val deleted = transactionMatchingRepository.deleteByIdIn(ids)
        transactionMatchingRepository.updateSequence()
        contribution.incrementWriteCount(deleted.toLong())
        return RepeatStatus.FINISHED
    }

}