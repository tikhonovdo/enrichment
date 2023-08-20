package ru.tikhonovdo.enrichment.old.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import ru.tikhonovdo.enrichment.domain.financepm.FinancePmDataHolder

@Deprecated(message = "till turned to service")
class InitDataRunner(private val dataHolder: FinancePmDataHolder): ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        dataHolder.initData(args)
    }
}