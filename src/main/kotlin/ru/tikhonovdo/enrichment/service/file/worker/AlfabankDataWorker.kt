package ru.tikhonovdo.enrichment.service.file.worker

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.dto.transaction.AlfaRecord
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.DraftTransactionRepository
import ru.tikhonovdo.enrichment.util.JsonMapper.Companion.JSON_MAPPER
import java.io.ByteArrayInputStream
import java.util.function.Predicate
import java.util.function.Supplier

@Component
class AlfabankDataWorker(
    draftTransactionRepository: DraftTransactionRepository,
    private val alfaFileWorkerResultFilter: Supplier<Predicate<DraftTransaction>> = Supplier { Predicate { true } }
): BankDataWorker(draftTransactionRepository, Bank.ALFA) {

    override fun readBytes(vararg content: ByteArray): List<DraftTransaction> {
        val workbook: Workbook = XSSFWorkbook(ByteArrayInputStream(content[0]))
        val sheet = workbook.getSheetAt(0)
        val rowIterator = sheet.iterator()
        if (rowIterator.hasNext()) {
            rowIterator.next() // skip header row
        }

        /* note: У Альфы нет времени совершения транзакции - только дата.
         * Это создает сложности, при вставке данных в драфты -- уникальность данных завязана на дату-время.
         * Поскольку данные выгружаются ДНЯМИ, можно заложиться на это и добавлять по секунде к operationDate и
         * быть уверенным в порядке транзакций внутри дня. Однако, если в парсер попадут данные за половину дня --
         * это потенциально может все сломать. Предохраниться от этого в самом воркере невозможно.
        */
        val records = mutableListOf<AlfaRecord.Raw>()
        rowIterator.forEachRemaining {  row ->
            AlfaRecord.Raw().apply {
                row.cellIterator().forEachRemaining { cell ->
                    when (cell.columnIndex) {
                        0  -> this.operationDate = cell.stringCellValue
                        1  -> this.paymentDate = cell.stringCellValue
                        2  -> this.accountName = cell.stringCellValue
                        3  -> this.accountNumber = cell.stringCellValue
                        4  -> this.cardName = cell.stringCellValue
                        5  -> this.cardNumber = cell.stringCellValue
                        6  -> this.description = cell.stringCellValue.replace(Regex("\\s+"), " ")
                        7  -> this.paymentSum = cell.numericCellValue
                        8  -> this.paymentCurrency = cell.stringCellValue
                        9  -> this.status = cell.stringCellValue
                        10 -> this.category = cell.stringCellValue
                        11 -> this.mcc = cell.numericCellValue.toInt().takeIf { it != 0 }
                        12 -> this.type = cell.stringCellValue
                        14 -> this.comment = cell.stringCellValue
                    }
                }
            }.let {
                if (!it.operationDate.isNullOrBlank()) {
                    records.add(it)
                }
            }
        }

        val result = records.reversed()
            .map { toDraftTransaction(it) }
            .filter { alfaFileWorkerResultFilter.get().test(it) }

        result.forEachIndexed { index, current ->
            val previous = if (index > 0) result[index - 1] else null
            fixLocalDateTime(current, previous)
        }

        return result.reversed()
    }

    private fun toDraftTransaction(record: AlfaRecord.Raw) = DraftTransaction(
        bankId = Bank.ALFA.id,
        date = AlfaRecord.parseRawDate(record.operationDate!!),
        sum = record.paymentSum.toString(),
        data = JSON_MAPPER.writeValueAsString(record)
    )

    private fun fixLocalDateTime(current: DraftTransaction, previous: DraftTransaction?) {
        if (previous != null && current.date.toLocalDate() == previous.date.toLocalDate()) {
            current.date = previous.date.plusSeconds(1)
        }
    }
}