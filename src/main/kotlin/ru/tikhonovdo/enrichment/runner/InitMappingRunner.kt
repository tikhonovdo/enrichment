package ru.tikhonovdo.enrichment.runner

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.config.MappingConfig
import ru.tikhonovdo.enrichment.financepm.AccountRecord
import ru.tikhonovdo.enrichment.financepm.CategoryRecord
import ru.tikhonovdo.enrichment.financepm.FinancePmData
import ru.tikhonovdo.enrichment.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.runner.InitMappingRunner.Companion.initMappingProfile
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists

@Profile(initMappingProfile)
class InitMappingRunner(
    private val financePmDataHolder: FinancePmDataHolder,
    private val mappingConfig: MappingConfig
): ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        createMappings(financePmDataHolder.data)
    }

    private fun createMappings(data: FinancePmData) {
        path(mappingConfig.categories)?.populateData(
            data.categories,
            "financePmCategoryId;financePmCategoryType;financePmCategoryName;mcc;tinkoffDescriptionPart"
        ) { category: CategoryRecord -> "${category.id};${category.type};${category.name};;" }

        path(mappingConfig.accounts)?.populateData(
            data.accounts,
            "cardNumber;financePmAccountId;financePmAccountName"
        ) { account: AccountRecord -> ";${account.id};${account.name}" }
    }

    private fun path(fileName: String): BufferedWriter? {
        val path = Paths.get(fileName)
        return if (path.exists()) {
            log.info("$path exists. Creating initial mapping aborted.")
            null
        } else {
            log.info("$path not exists. Creating initial mapping...")
            Files.createDirectories(path.parent)
            path.createFile().bufferedWriter(Charsets.UTF_8)
        }
    }

    private fun <T> BufferedWriter.populateData(collection: List<T>, header: String, rowProducer: (T) -> String) {
        this.append(header)
        collection.forEach {
            this.newLine()
            this.append(rowProducer.invoke(it))
        }
        this.flush()
        this.close()
    }

    companion object {
        const val initMappingProfile = "init-mapping"

        private val log: Logger = LoggerFactory.getLogger(InitMappingRunner::class.java)
    }

}