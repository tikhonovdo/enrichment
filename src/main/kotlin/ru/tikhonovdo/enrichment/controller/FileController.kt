package ru.tikhonovdo.enrichment.controller

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.service.file.RawDataService
import ru.tikhonovdo.enrichment.service.file.worker.SaveMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/file")
@Controller
class FileController(private val rawDataService: RawDataService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("file") file: MultipartFile, @RequestParam("mode") saveMode: SaveMode?) {
        rawDataService.saveData(file, saveMode ?: SaveMode.DEFAULT)
    }

    @GetMapping(produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun download(): ResponseEntity<Resource> {
        val resource = rawDataService.load()

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=financePM_${now()}.data")
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .body(resource);
    }

    private fun now(): String =
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

}
