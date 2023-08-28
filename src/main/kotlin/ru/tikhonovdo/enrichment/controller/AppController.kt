package ru.tikhonovdo.enrichment.controller

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.service.FileService
import java.time.LocalDate

@RestController
@RequestMapping("/")
@Controller
class AppController(
    private val fileService: FileService,
    private val jobLauncher: JobLauncher,
    private val matchingJob: Job
) {

    companion object {
        const val START_DATE_TIME = "dateTime"
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadData(@RequestParam("file") file: MultipartFile) {
        fileService.store(file)
    }

    @PostMapping("/performMatching")
    fun performMatching(@RequestParam(required = false) requestParam: Map<String, String>?): Boolean {
        val params = JobParametersBuilder()
        requestParam?.let {
            requestParam["steps"]?.let {
                params.addString("steps", it)
            }
        }
        jobLauncher.run(matchingJob, params.toJobParameters())
        return true
    }

    @GetMapping("/download")
    fun downloadData(): ResponseEntity<Resource> {
        val resource = fileService.load()

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=financePM_${LocalDate.now()}.data")
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

}
