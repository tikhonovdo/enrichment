package ru.tikhonovdo.enrichment.service.importscenario

enum class ScenarioState(val weight: Int, val stepName: String) {
    START(0, "start"),
    YA_PASSPORT_QR(1, "ya_login_qr"),
    OTP_SENT(11, "otp_sent"),
    LOGIN_SUCCEED(12, "login_succeed"),
    DATA_SAVED(13, "data_saved"),
    FAILURE(14, "failure"),

    DESTROYED(Int.MAX_VALUE, "");

}