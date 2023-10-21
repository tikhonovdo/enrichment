package ru.tikhonovdo.enrichment.batch.common

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import ru.tikhonovdo.enrichment.batch.common.StepExecutionDecider.Companion.CONTINUE
import ru.tikhonovdo.enrichment.batch.common.StepExecutionDecider.Companion.SKIP

class CustomFlowBuilder(private val flowName: String) {

    private val steps = mutableListOf<Step>()
    private val deciders = mutableListOf<StepExecutionDecider>()

    fun addStep(step: Step): CustomFlowBuilder {
        steps.add(step)
        deciders.add(StepExecutionDecider(step.name))
        return this
    }

    fun build(): Flow {
        var builder = FlowBuilder<Flow>(flowName)

        for (i in 0 until steps.indices.last) {
            val current = steps[i]
            val currentDecider = deciders[i]
            val nextDecider = deciders[i + 1]
            builder = builder
                .from(currentDecider).on(CONTINUE.name).to(current)
                .from(current).on("*").to(nextDecider)
                .from(currentDecider).on(SKIP.name).to(nextDecider)
        }
        val last = steps[steps.indices.last]
        val lastDecider = deciders[steps.indices.last]
        builder = builder
            .from(lastDecider).on(CONTINUE.name).to(last)
            .from(lastDecider).on(SKIP.name).end()

        return builder.build()

    }
}