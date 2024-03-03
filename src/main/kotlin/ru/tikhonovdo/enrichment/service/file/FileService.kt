package ru.tikhonovdo.enrichment.service.file

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.domain.FileType.*
import ru.tikhonovdo.enrichment.service.file.worker.AlfabankFileWorker
import ru.tikhonovdo.enrichment.service.file.worker.FileWorker
import ru.tikhonovdo.enrichment.service.file.worker.FinancePmFileWorker
import ru.tikhonovdo.enrichment.service.file.worker.TinkoffFileWorker

interface FileService {
    fun saveData(file: MultipartFile, fullReset: Boolean)
    fun saveData(fileType: FileType, fullReset: Boolean = false, vararg content: ByteArray)
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

    override fun saveData(file: MultipartFile, fullReset: Boolean) {
        val fileType = detectFileType(file)

        saveData(fileType, fullReset, file.resource.contentAsByteArray)
    }

    override fun saveData(fileType: FileType, fullReset: Boolean, vararg content: ByteArray) {
        workers[fileType]?.let {
            log.info("Recognized as $fileType data file")
            it.saveData(fullReset, *content)
            log.info("$fileType data file was successfully saved")
        }
    }

    override fun load() =
        ByteArrayResource(financePmFileWorker.retrieveData())

    private fun detectFileType(file: MultipartFile): FileType {
        if (file.originalFilename?.matches(Regex("finance(.*).data")) == true) {
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

}