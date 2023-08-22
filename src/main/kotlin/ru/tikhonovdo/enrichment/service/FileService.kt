package ru.tikhonovdo.enrichment.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.domain.FileType.*

interface FileService {
    fun store(file: MultipartFile)
    fun load() : Resource
}

@Service
class FileServiceImpl(
    val financePmService: FinancePmService,
    val tinkoffService: TinkoffService
) : FileService {

    private val log = LoggerFactory.getLogger(FileServiceImpl::class.java)

    override fun store(file: MultipartFile) {
        val fileType = detectFileType(file)

        when (fileType) {
            FINANCE_PM -> financePmService.saveData(file.resource.contentAsByteArray)
            TINKOFF -> tinkoffService.saveData(file)
        }

        log.info("$fileType data file was successfully saved")
    }

    override fun load() = ByteArrayResource(financePmService.retrieveData())

    private fun detectFileType(file: MultipartFile): FileType {
        if (file.originalFilename?.matches(Regex("finance(.*).data")) == true) {
            return FINANCE_PM
        }
        if ((file.contentType == "application/vnd.ms-excel" || file.contentType == "text/csv") &&
            file.originalFilename?.matches(Regex("operations(.*)")) == true) {
            return TINKOFF
        }
        throw IllegalStateException("unknown file type")
    }

}