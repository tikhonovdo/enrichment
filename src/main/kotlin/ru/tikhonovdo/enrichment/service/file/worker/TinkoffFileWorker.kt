package ru.tikhonovdo.enrichment.service.file.worker

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.io.ByteArrayInputStream

@Component
class TinkoffFileWorker(draftTransactionRepository: DraftTransactionRepository):
    BankFileWorker(draftTransactionRepository, Bank.TINKOFF) {

    override fun readFile(content: ByteArray): List<DraftTransaction> {
        val workbook: Workbook = HSSFWorkbook(ByteArrayInputStream(content))
        val sheet = workbook.getSheetAt(0)
        val rowIterator = sheet.iterator()
        if (rowIterator.hasNext()) {
            rowIterator.next() // skip header row
        }

        val records = mutableListOf<TinkoffRecord.Raw>()
        rowIterator.forEachRemaining {  row ->
            TinkoffRecord.Raw().apply {
                row.cellIterator().forEachRemaining { cell ->
                    when (cell.columnIndex) {
                        0  -> this.operationDate = cell.stringCellValue
                        1  -> this.paymentDate = cell.stringCellValue
                        2  -> this.cardNumber = cell.stringCellValue
                        3  -> this.status = cell.stringCellValue
                        4  -> this.operationSum = cell.numericCellValue
                        5  -> this.operationCurrency = cell.stringCellValue
                        6  -> this.paymentSum = cell.numericCellValue
                        7  -> this.paymentCurrency = cell.stringCellValue
                        8  -> this.cashback = cell.numericCellValue
                        9  -> this.category = cell.stringCellValue
                        10 -> this.mcc = cell.numericCellValue.toInt()
                        11 -> this.description = cell.stringCellValue
                        12 -> this.totalBonuses = cell.numericCellValue
                        13 -> this.roundingForInvestKopilka = cell.numericCellValue
                        14 -> this.sumWithRoundingForInvestKopilka = cell.numericCellValue
                    }
                }
            }.let {
                records.add(it)
            }
        }
        return records.map { toDraftTransaction(it) }
    }

    private fun toDraftTransaction(record: TinkoffRecord.Raw) = DraftTransaction(
        bankId = Bank.TINKOFF.id,
        date = TinkoffRecord.parseOperationDate(record.operationDate!!),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )
}

