package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDateTime
import java.time.ZoneId

class NullAsZeroSerializer: JsonSerializer<Long?>() {
    override fun serialize(value: Long?, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeNumber(value ?: 0L)
    }
}

class LocalDateTimeToMillisSerializer: JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, generator: JsonGenerator, provider: SerializerProvider) {
        val millis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        generator.writeString(millis.toString())
    }
}