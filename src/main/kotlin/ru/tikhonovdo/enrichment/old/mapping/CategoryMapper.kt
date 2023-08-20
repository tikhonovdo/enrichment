package ru.tikhonovdo.enrichment.old.mapping

import org.apache.commons.csv.CSVFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.tikhonovdo.enrichment.domain.enitity.Category
import ru.tikhonovdo.enrichment.domain.financepm.FinancePmDataHolder
import ru.tikhonovdo.enrichment.old.tinkoff.TinkoffRecord
import kotlin.math.sign

/**
 * Занимается сопоставлением идентификаторов категорий FinancePM с TinkoffRecord
 * @see TinkoffRecord
 * @see CategoryRecord
 */
class CategoryMapper(
    fileName: String,
    csvFormat: CSVFormat,
    private val financePmDataHolder: FinancePmDataHolder
) : AbstractFinancePmIdMapper(fileName, csvFormat) {
    private val categoriesList = mutableListOf<CategoryMatchingRecord>();
    private val log: Logger = LoggerFactory.getLogger(CategoryMapper::class.java)

    init {

        for (record in csv) {
            categoriesList.add(
                CategoryMatchingRecord(
                    record.get("financePmCategoryId").toInt(),
                    record.get("financePmCategoryName"),
                    record.get("financePmCategoryType").toInt() == 1,
                    record.get("mcc"),
                    record.get("tinkoffDescriptionPart")
                )
            )
        }
    }

    /**
     * Сопоставляет идентификаторы категорий FinancePM
     * @see CategoryRecord
     */
    override fun getFinancePmId(tinkoffRecord: TinkoffRecord): Int? {
        //firstly trying to find by category name directly (it's name must be unique for this method)
        val categoryRecord = findCategoryIdByName(tinkoffRecord.category);

        return categoryRecord?.id?.toInt()
        // if category is not found - trying to apply heuristic rules
            ?: getCategoryMatchingRecord(tinkoffRecord)?.financePmCategoryId
    }

    fun findCategoryIdByName(categoryName: String): Category? {
        val financePmCategories = financePmDataHolder.data.categories
        financePmCategories.filter { category -> category.name == categoryName }
            .forEach { log.info("found $it") }
        return null // financePmCategories.firstOrNull { category -> category.name == categoryName }
    }

    private fun getCategoryMatchingRecord(tinkoffRecord: TinkoffRecord): CategoryMatchingRecord? {
        if (categoryIsNotProcessable(tinkoffRecord)) {
            log.info("cash and transfers need to be processed alternately")
            return null
        }

        val income = tinkoffRecord.paymentSum.sign > 0
        val filteredByType = categoriesList.filter { it.income == income }

        // firstly filtering by mcc
        val filteredByMcc = filteredByType.filter {
            it.mcc == tinkoffRecord.mcc
        }
        //if mcc is not matched - try to find by description substring
        if (filteredByMcc.isEmpty()) {
            val filteredByDescription = filteredByType
                .filter {
                    it.tinkoffDescriptionPart.isNotEmpty() && tinkoffRecord.description.contains(it.tinkoffDescriptionPart) ||
                            tinkoffRecord.category.contains(it.financePmCategoryName)
                }

            if (filteredByDescription.isNotEmpty()) {
                log.debug("record found by description")

                return filteredByDescription.first()
            }

            log.warn("record not found by mcc and description: $tinkoffRecord")
            return filteredByType.first { it.financePmCategoryName == "Другое" }
        } else {
            // try additional filtering by description part
            val filteredByMccAndDescription = filteredByMcc
                .filter {
                    it.tinkoffDescriptionPart.isNotEmpty() &&
                            tinkoffRecord.description.lowercase().contains(it.tinkoffDescriptionPart.lowercase()) ||
                            tinkoffRecord.category.contains(it.financePmCategoryName)
                }
            if (filteredByMccAndDescription.isNotEmpty()) {
                log.debug("record found by mcc and description")
                return filteredByMccAndDescription.first()
            } else {
                if (tinkoffRecord.category.contains("Переводы")) {
                    log.info("Transfer must be processed manually: $tinkoffRecord")
                    return null
                }

                log.debug("record found by mcc")
                return filteredByMcc.first()
            }
        }
    }


    private fun categoryIsNotProcessable(tinkoffRecord: TinkoffRecord): Boolean {
        return tinkoffRecord.category.contains("Наличные") ||
                tinkoffRecord.description.contains("Перевод")
    }

}

class CategoryMatchingRecord(
    val financePmCategoryId: Int,
    val financePmCategoryName: String,
    val income: Boolean,
    val mcc: String,
    val tinkoffDescriptionPart: String
)