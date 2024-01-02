package ru.tikhonovdo.enrichment.service.importscenario

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSZ]")
fun sixMonthsAgo(): LocalDateTime = LocalDateTime.of(LocalDate.now().minusMonths(6), LocalTime.MIN)