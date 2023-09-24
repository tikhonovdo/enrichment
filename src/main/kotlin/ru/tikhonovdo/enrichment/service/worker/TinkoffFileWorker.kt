package ru.tikhonovdo.enrichment.service.worker

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.service.FileServiceWorker
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.io.ByteArrayInputStream

@Component
class TinkoffFileWorker(private val draftTransactionRepository: DraftTransactionRepository): FileServiceWorker {

    private val log = LoggerFactory.getLogger(TinkoffFileWorker::class.java)

    @Transactional
    override fun saveData(file: MultipartFile, fullReset: Boolean) {
        val rawRecords = readExcelFile(file.resource.contentAsByteArray)
        val tinkoffDrafts = draftTransactionRepository.findAllByBankId(Bank.TINKOFF.id)

        rawRecords.map { toDraftTransaction(it) }.filter {
            !tinkoffDrafts.contains(it)
        }.let {
            var inserted = 0
            if (it.isNotEmpty()) {
                inserted = draftTransactionRepository.insertBatch(it)
            }
            log.info("Upload success. $inserted records was inserted")
        }
    }

    private fun readExcelFile(contentAsByteArray: ByteArray): List<TinkoffRecord.Raw> {
        val workbook: Workbook = HSSFWorkbook(ByteArrayInputStream(contentAsByteArray))
        val sheet = workbook.getSheetAt(0);
        val rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next() // skip header row
        }

        val records = mutableListOf<TinkoffRecord.Raw>()
        rowIterator.forEachRemaining {  row ->
            val rawTinkoffRecord = TinkoffRecord.Raw().apply {
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
            }
            records.add(rawTinkoffRecord)
        }
        return records
    }

    private fun toDraftTransaction(record: TinkoffRecord.Raw) = DraftTransaction(
        bankId = Bank.TINKOFF.id,
        date = TinkoffRecord.parseOperationDate(record.operationDate!!),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )
}

