package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.*

@Repository
interface DraftTransactionRepository : JpaRepository<DraftTransaction, Long> {
}

