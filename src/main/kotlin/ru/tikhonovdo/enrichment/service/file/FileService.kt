package ru.tikhonovdo.enrichment.service.file

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.domain.FileType.*
import ru.tikhonovdo.enrichment.service.file.worker.*

interface FileService {
    fun saveData(file: MultipartFile, saveMode: SaveMode)
    fun saveData(fileType: FileType, saveMode: SaveMode = SaveMode.DEFAULT, vararg content: ByteArray)
    fun load() : Resource
}

@Service
class FileServiceImpl(
    private val financePmFileWorker: FinancePmFileWorker,
    tinkoffFileWorker: TinkoffFileWorker,
    alfabankFileWorker: AlfabankFileWorker
) : FileService {

    private val log = LoggerFactory.getLogger(FileServiceImpl::class.java)
    private val workers = mutableMapOf<FileType, FileWorker>()

    init {
        workers[FINANCE_PM] = financePmFileWorker
        workers[TINKOFF] = tinkoffFileWorker
        workers[ALFA] = alfabankFileWorker
    }

    override fun saveData(file: MultipartFile, saveMode: SaveMode) {
        val fileType = detectFileType(file)

        saveData(fileType, saveMode, file.resource.contentAsByteArray)
    }

    override fun saveData(fileType: FileType, saveMode: SaveMode, vararg content: ByteArray) {
        workers[fileType]?.let {
            log.info("Recognized as $fileType data file")
            it.saveData(saveMode, *content)
            log.info("$fileType data file was successfully saved")
        }
    }

    override fun load() =
        ByteArrayResource(financePmFileWorker.retrieveData())

    private fun detectFileType(file: MultipartFile): FileType {
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