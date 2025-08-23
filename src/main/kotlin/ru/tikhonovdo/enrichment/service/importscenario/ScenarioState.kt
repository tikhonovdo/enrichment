package ru.tikhonovdo.enrichment.service.importscenario

enum class ScenarioState(val weight: Int, val stepName: String) {
    START(0, "start"),
    YA_PASSPORT_QR(1, "ya_login_qr"),
    OTP_SENT(11, "otp_sent"),
    LOGIN_SUCCEED(12, "login_succeed"),
    COOKIE_RECEIVED(13, "cookie_received"),
    TERMINATE(20, "terminate_state"), // service state, do not use in client logic
    DATA_SAVED(21, "data_saved"),
    FAILURE(30, "failure"),

    DESTROYED(Int.MAX_VALUE, "");

}