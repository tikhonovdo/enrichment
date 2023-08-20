package ru.tikhonovdo.enrichment.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import ru.tikhonovdo.enrichment.repository.*
import ru.tikhonovdo.enrichment.service.FileServiceImpl.FileType.*

interface FileService {
    fun store(file: MultipartFile, bankId: Long?)
    fun load() : Resource
}

@Service
class FileServiceImpl(
    val draftTransactionRepository: DraftTransactionRepository,
    val financePmService: FinancePmService
) : FileService {

    private val log = LoggerFactory.getLogger(FileServiceImpl::class.java)

    override fun store(file: MultipartFile, bankId: Long?) {
        val fileType = detectFileType(file)

        when (fileType) {
            FINANCE_PM -> financePmService.saveData(file.resource.contentAsByteArray)

            else -> draftTransactionRepository.save(DraftTransaction(null, fileType.bankId!!, file.name, file.resource.contentAsByteArray))
        }

        log.info("$fileType data file was successfully saved")
    }

    override fun load() = ByteArrayResource(financePmService.retrieveData())

    private fun detectFileType(file: MultipartFile): FileType {
        if (file.originalFilename?.matches(Regex("finance(.*)data")) == true) {
            return FINANCE_PM
        }
        return TINKOFF
    }

    enum class FileType(val bankId: Long?) {
        FINANCE_PM(null),
        TINKOFF(1)
    }
}