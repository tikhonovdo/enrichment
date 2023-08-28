package ru.tikhonovdo.enrichment.repository.matching

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.repository.AbstractBatchRepository
import ru.tikhonovdo.enrichment.repository.BatchRepository

interface CategoryMatchingRepository:
    JpaRepository<CategoryMatching, CategoryMatching.CategoryMatchingId>,
    CustomCategoryMatchingRepository

interface CustomCategoryMatchingRepository: BatchRepository<CategoryMatching>

@Repository
class CustomCategoryMatchingRepositoryImpl(namedParameterJdbcTemplate: NamedParameterJdbcTemplate):
    CustomCategoryMatchingRepository,
    AbstractBatchRepository<CategoryMatching>(
        namedParameterJdbcTemplate,
        "INSERT INTO category_matching (category_id, bank_id, bank_category_name, mcc, pattern, validated) " +
                "VALUES (:categoryId, :bankId, :bankCategoryName, :mcc, :pattern, :validated)"
    )