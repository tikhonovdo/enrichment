package ru.tikhonovdo.enrichment.config

import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.tikhonovdo.enrichment.util.StringToBankEnumConverter

@Configuration
class WebConfig: WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToBankEnumConverter())
    }
}