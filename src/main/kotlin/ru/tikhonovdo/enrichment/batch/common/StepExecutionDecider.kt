package ru.tikhonovdo.enrichment.batch.common

import org.springframework.batch.core.*
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider

class StepExecutionDecider(private val stepName: String): JobExecutionDecider {

    companion object {
        const val INCLUDED_STEPS = "includedSteps"
        const val EXCLUDED_STEPS = "excludedSteps"
        val SKIP = FlowExecutionStatus("SKIP")
        val CONTINUE = FlowExecutionStatus("CONTINUE")
    }

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        return if (stepExecution?.exitStatus?.exitCode == ExitStatus.FAILED.exitCode) {
            FlowExecutionStatus.FAILED
        } else {
            if (needExecute(jobExecution)) {
                CONTINUE
            } else {
                SKIP
            }
        }
    }

    private fun needExecute(jobExecution: JobExecution): Boolean =
        if (BatchStatus.FAILED == jobExecution.status) {
            false
        } else {
            isStepExecutable(jobExecution.jobParameters)
        }

    private fun isStepExecutable(jobParameters: JobParameters): Boolean {
        val excludedSteps = jobParameters.getString(EXCLUDED_STEPS)?.split(",").orEmpty()
        val includedSteps = jobParameters.getString(INCLUDED_STEPS)?.split(",").orEmpty()

        val stepExcluded =
            if (excludedSteps.isNotEmpty()) {
                excludedSteps.contains(stepName)
            } else {
                false
            }
        if (stepExcluded) return false

        return if (includedSteps.isNotEmpty()) {
            includedSteps.contains(stepName)
        } else {
            true
        }
    }
}