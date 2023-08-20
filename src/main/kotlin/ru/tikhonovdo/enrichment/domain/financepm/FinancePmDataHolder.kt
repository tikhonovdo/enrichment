package ru.tikhonovdo.enrichment.domain.financepm

import com.beust.klaxon.Klaxon
import org.springframework.boot.ApplicationArguments
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.dto.FinancePmData
import java.nio.file.Path
import java.util.stream.Stream

@Deprecated(message = "till turned to service")
@Component
class FinancePmDataHolder(private val klaxon: Klaxon) {

    var data: FinancePmData = FinancePmData()

    fun initData(args: ApplicationArguments) {
        if (data.isEmpty) {
            data = parseData(args.getDataFilePath())
        } else {
            throw IllegalStateException("Cannot initialize data twice")
        }
    }

    private fun parseData(financePmSourcePath: Path): FinancePmData =
        klaxon.parse<FinancePmData>(financePmSourcePath.toFile())!!

    fun validate() {
        data.transactions.forEach{ transaction ->
            if (Stream.of(transaction.categoryId, transaction.accountId, transaction.typeId, transaction.eventId).anyMatch { it == null })
                throw IllegalStateException("$transaction has null fields!")
        }
    }
}
