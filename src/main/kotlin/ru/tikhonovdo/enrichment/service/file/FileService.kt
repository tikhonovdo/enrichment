package ru.tikhonovdo.enrichment.service.file

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.domain.FileType.*
import ru.tikhonovdo.enrichment.service.file.worker.FinancePmFileWorker
import ru.tikhonovdo.enrichment.service.file.worker.TinkoffFileWorker

interface FileService {
    fun saveData(file: MultipartFile, fullReset: Boolean)
    fun saveData(content: ByteArray, fileType: FileType, fullReset: Boolean = false)
    fun load() : Resource
}

interface FileServiceWorker {
    fun saveData(content: ByteArray, fullReset: Boolean = false)
}

@Service
class FileServiceImpl(
    private val financePmFileWorker: FinancePmFileWorker,
    tinkoffFileWorker: TinkoffFileWorker
) : FileService {

    private val log = LoggerFactory.getLogger(FileServiceImpl::class.java)
    private val workers = mutableMapOf<FileType, FileServiceWorker>()

    init {
        workers[FINANCE_PM] = financePmFileWorker
        workers[TINKOFF] = tinkoffFileWorker
    }

    override fun saveData(file: MultipartFile, fullReset: Boolean) {
        val fileType = detectFileType(file)

        saveData(file.resource.contentAsByteArray, fileType, fullReset)
    }

    override fun saveData(content: ByteArray, fileType: FileType, fullReset: Boolean) {
        workers[fileType]?.let {
            log.info("Recognized as $fileType data file")
            it.saveData(content, fullReset)
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
        throw IllegalStateException("unknown file type")
    }

}