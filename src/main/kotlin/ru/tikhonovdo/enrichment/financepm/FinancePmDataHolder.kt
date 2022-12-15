package ru.tikhonovdo.enrichment.financepm

import com.beust.klaxon.Klaxon
import org.springframework.boot.ApplicationArguments
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class FinancePmDataHolder(private val klaxon: Klaxon) {

    var data: FinancePmData = FinancePmData()

    fun initData(args: ApplicationArguments) {
        if (data.isEmpty) {
            data = parseData(args.nonOptionArgs[0])
        } else {
            throw IllegalStateException("Cannot initialize data twice")
        }
    }

    private fun parseData(financePmSourcePath: String?): FinancePmData =
        if (financePmSourcePath != null) {
            klaxon.parse<FinancePmData>(Paths.get(financePmSourcePath).toFile())!!
        } else {
            throw IllegalStateException("Source path for FinancePm data file is null")
        }
}
