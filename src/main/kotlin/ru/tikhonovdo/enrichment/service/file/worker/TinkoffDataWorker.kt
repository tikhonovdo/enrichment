package ru.tikhonovdo.enrichment.service.file.worker

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsAdditionalDataPayload
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffOperationsAdditionalDataRecord
import ru.tikhonovdo.enrichment.domain.dto.transaction.tinkoff.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.abs

@Component
class TinkoffDataWorker(draftTransactionRepository: DraftTransactionRepository):
    BankDataWorker(draftTransactionRepository, Bank.TINKOFF) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        // todo: make 'additionalDataRecords' the only data source - get rid of xlsReport
        val additionalDataRecords = if (content.size > 1) parseAdditionalData(content[1]) else emptyList()
        val rawRecords = readXlsReport(content[0], additionalDataRecords)

        return rawRecords.map { toDraftTransaction(it) }
    }

    private fun readXlsReport(xlsDocument: ByteArray, additionalDataRecords: List<TinkoffOperationsAdditionalDataRecord>?): List<TinkoffRecord.Raw> {
        val workbook: Workbook = HSSFWorkbook(ByteArrayInputStream(xlsDocument))
        val sheet = workbook.getSheetAt(0)
        val rowIterator = sheet.iterator()
        if (rowIterator.hasNext()) {
            rowIterator.next() // skip header row
        }

        val records = mutableListOf<TinkoffRecord.Raw>()
        rowIterator.forEachRemaining { row ->
            TinkoffRecord.Raw().apply {
                row.cellIterator().forEachRemaining { cell ->
                    when (cell.columnIndex) {
                        0 -> this.operationDate = cell.stringCellValue
                        1 -> this.paymentDate = cell.stringCellValue
                        2 -> this.cardNumber = cell.stringCellValue
                        3 -> this.status = cell.stringCellValue
                        4 -> this.operationSum = cell.numericCellValue
                        5 -> this.operationCurrency = cell.stringCellValue
                        6 -> this.paymentSum = cell.numericCellValue
                        7 -> this.paymentCurrency = cell.stringCellValue
                        8 -> this.cashback = cell.numericCellValue
                        9 -> this.category = cell.stringCellValue
                        10 -> this.mcc = cell.numericCellValue.toInt()
                        11 -> this.description = cell.stringCellValue
                        12 -> this.totalBonuses = cell.numericCellValue
                        13 -> this.roundingForInvestKopilka = cell.numericCellValue
                        14 -> this.sumWithRoundingForInvestKopilka = cell.numericCellValue
                    }
                }

                additionalDataRecords?.find {
                    TinkoffRecord.parseOperationDateToInstant(operationDate!!) == Instant.ofEpochMilli(it.operationTime!!) &&
                    abs(operationSum!!) == it.accountAmount &&
                    (cardNumber == null || it.cardNumber?.endsWith(cardNumber!!) == true)
                }?.let {
                    accountNumber = it.account
                    message = it.message
                    brandName = it.brandName
                }

            }.let {
                records.add(it)
            }
        }
        return deduplicate(records)
    }

    private fun parseAdditionalData(jsonPayloadBytes: ByteArray): List<TinkoffOperationsAdditionalDataRecord>? {
        val data = JSON_MAPPER.readValue(jsonPayloadBytes, TinkoffOperationsAdditionalDataPayload::class.java)
        return if (data.resultCode == "OK") {
            data.payload
        } else {
            null
        }
    }

    private fun toDraftTransaction(record: TinkoffRecord.Raw) = DraftTransaction(
        bankId = Bank.TINKOFF.id,
        date = LocalDateTime.ofInstant(TinkoffRecord.parseOperationDateToInstant(record.operationDate!!), ZoneId.of("UTC")),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )

    private fun deduplicate(records: MutableList<TinkoffRecord.Raw>): List<TinkoffRecord.Raw> {
        val result = records.groupBy { it.operationDate }
            .filter { it.value.size == 1 }.values
            .flatten()
            .toMutableList()

        val duplicatesByDate = records.groupBy { it.operationDate }
            .filter { it.value.size > 1 }

        duplicatesByDate
            .forEach { (_, duplicatedByDateList) ->
                duplicatedByDateList
                    .forEachIndexed { index, raw ->
                        val instant = TinkoffRecord.parseOperationDateToInstant(raw.operationDate!!)
                        val fixedLocalDateTime = LocalDateTime.ofInstant(instant.plusMillis(index.toLong()), ZoneOffset.ofHours(3))
                        result.add(raw.copy(
                            operationDate = TinkoffRecord.operationDateTimeFormatter.format(fixedLocalDateTime),
                        ))
                }
            }

        return result.sortedBy { it.operationDate }
    }
}

