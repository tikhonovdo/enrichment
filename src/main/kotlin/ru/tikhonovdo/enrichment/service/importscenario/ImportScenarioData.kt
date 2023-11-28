package ru.tikhonovdo.enrichment.service.importscenario

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ImportScenarioData(
    var login: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var otpCode: String? = null
)