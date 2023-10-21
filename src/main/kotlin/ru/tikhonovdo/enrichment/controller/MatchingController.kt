package ru.tikhonovdo.enrichment.controller

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/matching")
@Controller
class MatchingController(
    private val jobLauncher: JobLauncher,
    private val matchingJob: Job
) {
    @PostMapping("/perform")
    fun performMatching(@RequestParam(required = false) requestParam: Map<String, String>?): Boolean {
        val params = JobParametersBuilder()
        requestParam?.let {
            requestParam["steps"]?.let {
                params.addString("steps", it)
            }
        }
        jobLauncher.run(matchingJob, params.toJobParameters())
        return true
    }
}
