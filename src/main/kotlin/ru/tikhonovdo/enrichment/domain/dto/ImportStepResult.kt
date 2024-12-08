package ru.tikhonovdo.enrichment.domain.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState
import ru.tikhonovdo.enrichment.util.ScenarioStateDeserializer
import ru.tikhonovdo.enrichment.util.ScenarioStateSerializer

class ImportStepResult(
    @JsonSerialize(using = ScenarioStateSerializer::class)
    @JsonDeserialize(using = ScenarioStateDeserializer::class)
    var state: ScenarioState,

    var payload: Any? = null
)