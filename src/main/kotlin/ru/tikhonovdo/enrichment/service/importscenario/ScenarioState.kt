package ru.tikhonovdo.enrichment.service.importscenario

enum class ScenarioState(val weight: Int) {
    INITIAL(0),
    OTP_SENT(1),
    LOGIN_SUCCEED(2),
    DATA_SAVED(3),
    FAILURE(4),

    DESTROYED(Int.MAX_VALUE);

}