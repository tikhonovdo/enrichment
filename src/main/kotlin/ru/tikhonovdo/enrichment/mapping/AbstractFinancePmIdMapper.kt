package ru.tikhonovdo.enrichment.mapping

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import ru.tikhonovdo.enrichment.tinkoff.TinkoffRecord
import java.nio.file.Paths
import kotlin.io.path.bufferedReader

abstract class AbstractFinancePmIdMapper(fileName: String, csvFormat: CSVFormat) {
    protected val csv = CSVParser(Paths.get(fileName).bufferedReader(), csvFormat)
    abstract fun getFinancePmId(tinkoffRecord: TinkoffRecord): Int?
}