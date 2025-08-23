package ru.tikhonovdo.enrichment.service.file

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.DataType.FINANCE_PM
import ru.tikhonovdo.enrichment.service.file.worker.FinancePmDataWorker
import ru.tikhonovdo.enrichment.service.file.worker.SaveMode

interface RawDataService {
    fun saveData(file: MultipartFile, saveMode: SaveMode)
    fun load() : Resource
}

@Service
class RawDataServiceImpl(private val financePmDataWorker: FinancePmDataWorker) : RawDataService {

    private val log = LoggerFactory.getLogger(RawDataServiceImpl::class.java)

    override fun saveData(file: MultipartFile, saveMode: SaveMode) {
        financePmDataWorker.saveData(saveMode, file.resource.contentAsByteArray)
        log.info("$FINANCE_PM data file was successfully saved")
    }

    override fun load() = ByteArrayResource(financePmDataWorker.retrieveData())

}