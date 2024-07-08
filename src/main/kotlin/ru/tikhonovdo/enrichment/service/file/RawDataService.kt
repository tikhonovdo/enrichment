package ru.tikhonovdo.enrichment.service.file

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.DataType
import ru.tikhonovdo.enrichment.domain.DataType.*
import ru.tikhonovdo.enrichment.service.file.worker.*

interface RawDataService {
    fun saveData(file: MultipartFile, saveMode: SaveMode)
    fun saveData(dataType: DataType, saveMode: SaveMode = SaveMode.DEFAULT, vararg content: ByteArray)
    fun load() : Resource
}

@Service
class RawDataServiceImpl(
    private val financePmFileWorker: FinancePmDataWorker,
    tinkoffFileWorker: TinkoffDataWorker,
    alfabankFileWorker: AlfabankDataWorker,
    yandexFileWorker: YandexDataWorker
) : RawDataService {

    private val log = LoggerFactory.getLogger(RawDataServiceImpl::class.java)
    private val workers = mutableMapOf<DataType, DataWorker>()

    init {
        workers[FINANCE_PM] = financePmFileWorker
        workers[TINKOFF] = tinkoffFileWorker
        workers[ALFA] = alfabankFileWorker
        workers[YANDEX] = yandexFileWorker
    }

    override fun saveData(file: MultipartFile, saveMode: SaveMode) {
        val dataType = detectDataType(file)

        saveData(dataType, saveMode, file.resource.contentAsByteArray)
    }

    override fun saveData(dataType: DataType, saveMode: SaveMode, vararg content: ByteArray) {
        workers[dataType]?.let {
            log.info("Recognized as $dataType data file")
            it.saveData(saveMode, *content)
            log.info("$dataType data file was successfully saved")
        }
    }

    override fun load() =
        ByteArrayResource(financePmFileWorker.retrieveData())

    private fun detectDataType(file: MultipartFile): DataType {
        if (isFinancePm(file)) {
            return FINANCE_PM
        }
        if (file.originalFilename?.matches(Regex("operations(.*)")) == true) {
            return TINKOFF
        }
        if (file.originalFilename?.matches(Regex("Statement(.*).xlsx")) == true) {
            return ALFA
        }
        throw IllegalStateException("unknown file type")
    }

    private fun isFinancePm(file: MultipartFile): Boolean {
        return file.originalFilename?.matches(Regex("(.*).data")) == true
                && String(file.bytes.sliceArray(1..11)) == "\"version\":2"
    }

}