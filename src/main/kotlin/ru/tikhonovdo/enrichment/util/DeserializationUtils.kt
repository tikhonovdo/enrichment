package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.databind.JsonNode
import java.util.function.Function

fun <T> JsonNode.getField(fieldPath: String, defaultValue: T, function: Function<JsonNode, T>): T {
    val path = fieldPath.split(".")
    var currentNode = this

    for (fieldName in path) {
        if (!currentNode.isNull && currentNode.has(fieldName)) {
            currentNode = currentNode.get(fieldName)
        } else {
            return defaultValue
        }
    }

    return if (!currentNode.isNull) {
        function.apply(currentNode)
    } else {
        defaultValue
    }
}

fun JsonNode.getDoubleValue(fieldPath: String) = getField(fieldPath, 0.0) { it.doubleValue() }
fun JsonNode.getIntValue(fieldPath: String) = getField(fieldPath, 0) { it.intValue() }
fun JsonNode.getLongValue(fieldPath: String) = getField(fieldPath, 0L) { it.longValue() }
fun JsonNode.getTextValue(fieldPath: String) = getField(fieldPath, "") { it.textValue() }
fun JsonNode.getBooleanValue(fieldPath: String) = getField(fieldPath, false) { it.booleanValue() }
