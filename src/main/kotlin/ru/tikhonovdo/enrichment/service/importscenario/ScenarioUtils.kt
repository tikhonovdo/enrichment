package ru.tikhonovdo.enrichment.service.importscenario

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter

val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSZ]")
fun periodAgo(period: Period): LocalDateTime = LocalDateTime.of(LocalDate.now().minus(period), LocalTime.MIN)