package ru.tikhonovdo.enrichment.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder

@Component
@Order(2)
class InitDataRunner(private val dataHolder: FinancePmDataHolder): ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        dataHolder.initData(args)
    }
}