package ru.tikhonovdo.enrichment.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.config.MappingConfig
import ru.tikhonovdo.enrichment.runner.InitMappingRunner.Companion.initMappingProfile
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.notExists

@Component
@Order(1)
@Profile("!$initMappingProfile")
class ValidationRunner(
    private val mappingConfig: MappingConfig
): ApplicationRunner {

    private val errorMsg = "You must specify source data-file in FinancePM format"

    override fun run(args: ApplicationArguments?) {
        if (args == null) {
            throw IllegalArgumentException(errorMsg)
        }

        checkDataFileExistence(args)
        checkOptionsAllowance(args)
        validateMappingsExistence()
    }

    private fun checkDataFileExistence(args: ApplicationArguments) {
        val filePaths = args.nonOptionArgs

        val financePmDataFilePath = filePaths[0]
        if (Paths.get(financePmDataFilePath).notExists()) {
            throw IllegalArgumentException("$financePmDataFilePath not exists. $errorMsg")
        }

        filePaths.slice(1 until filePaths.size).forEach {
            if (Paths.get(it).notExists()) {
                throw IllegalArgumentException("$it not exists. Operations file must be in CSV format and not null")
            }
        }
    }

    private fun checkOptionsAllowance(args: ApplicationArguments) {
        val allowedOption = initMappingProfile
        if (!args.containsOption(allowedOption) &&
            args.optionNames.size > 0) {
            throw IllegalArgumentException("Only '--$allowedOption' option is allowed.")
        }
    }

    private fun validateMappingsExistence() {
        fun validatePath(fileName: String) {
            val path = Paths.get(fileName)
            if (!path.exists()) {
                throw IllegalStateException("$fileName was not exist. Use --${InitMappingRunner.initMappingProfile} key")
            }
        }
        validatePath(mappingConfig.accounts)
        validatePath(mappingConfig.categories)
    }
}