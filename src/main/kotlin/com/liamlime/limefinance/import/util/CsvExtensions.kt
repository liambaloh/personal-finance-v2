package com.liamlime.limefinance.import.util

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema

fun List<String>.removeCsvHeaderIfExists(schema: CsvSchema): List<String> {
    if (this.isEmpty()) return this

    val mapper = CsvMapper()
    val firstLineElements = mapper
        .readerForListOf(String::class.java)
        .with(CsvParser.Feature.WRAP_AS_ARRAY)
        .readValues<List<String>?>(this[0])
        .readAll()
        .toList()

    return if (firstLineElements == schema.columnNames.toList()) {
        this
    } else {
        this.drop(1)
    }
}