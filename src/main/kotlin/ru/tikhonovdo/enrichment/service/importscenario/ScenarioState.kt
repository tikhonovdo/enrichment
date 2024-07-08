package ru.tikhonovdo.enrichment.service.importscenario

enum class ScenarioState(val weight: Int, val stepName: String) {
    INITIAL(0, "start"),
    YA_PASSPORT_OTP_SENT(1, "ya_login_otp_sent"),
    YA_PASSPORT_LOGIN_SUCCEED(2, "ya_login_succeed"),
    OTP_SENT(11, "otp_sent"),
    LOGIN_SUCCEED(12, "login_succeed"),
    DATA_SAVED(13, "data_saved"),
    FAILURE(14, ""),

    DESTROYED(Int.MAX_VALUE, "");

}