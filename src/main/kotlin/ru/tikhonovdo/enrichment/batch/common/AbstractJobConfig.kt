package ru.tikhonovdo.enrichment.batch.common

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.FlowStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder

abstract class AbstractJobConfig(
    private val jobRepository: JobRepository,
) {

    fun flowStep(flow: Flow): Step =
        FlowStepBuilder(step(flow.name)).flow(flow).build()

    fun step(name: String): StepBuilder =
        StepBuilder(name, jobRepository)
            .allowStartIfComplete(true)
//            .listener(StepExecutionListener)

    fun job(name: String): JobBuilder = JobBuilder(name, jobRepository)

}