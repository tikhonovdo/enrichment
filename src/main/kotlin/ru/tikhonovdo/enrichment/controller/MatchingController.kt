package ru.tikhonovdo.enrichment.controller

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import ru.tikhonovdo.enrichment.batch.common.StepExecutionDecider
import ru.tikhonovdo.enrichment.repository.matching.TransactionMatchingRepository

@RestController
@RequestMapping("/matching")
@Controller
class MatchingController(
    private val jobLauncher: JobLauncher,
    private val matchingJob: Job,
    private val transactionMatchingRepository: TransactionMatchingRepository
) {
    @PostMapping
    fun performMatching(@RequestParam(required = false) requestParam: Map<String, String>?) {
        val params = JobParametersBuilder().apply {
            addLong("time", System.currentTimeMillis())
            requestParam?.get(StepExecutionDecider.INCLUDED_STEPS)?.let {
                addString(StepExecutionDecider.INCLUDED_STEPS, it)
            }
            requestParam?.get(StepExecutionDecider.EXCLUDED_STEPS)?.let {
                addString(StepExecutionDecider.EXCLUDED_STEPS, it)
            }
        }
        jobLauncher.run(matchingJob, params.toJobParameters())
    }

    @GetMapping("/count")
    fun getUnmatchedRecordsCount(): Int {
        return transactionMatchingRepository.getUnmatchedTransactionIds().size
    }
}
