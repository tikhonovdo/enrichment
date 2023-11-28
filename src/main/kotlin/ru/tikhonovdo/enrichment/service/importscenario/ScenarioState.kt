package ru.tikhonovdo.enrichment.service.importscenario

import ru.tikhonovdo.enrichment.domain.Bank

enum class ScenarioState(val bank: Bank?, val weight: Int) {
    INITIAL(null, 0),
    TINKOFF_OTP_SENT(Bank.TINKOFF, 1),
    TINKOFF_LOGIN_SUCCEED(Bank.TINKOFF, 2),

    DESTROYED(null, Int.MAX_VALUE);

}