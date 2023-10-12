package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.CategoryImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportCategories {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(CategoryImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<CategoryImportModel> {
        val categoriesCsv = readFileFromResources("/categories.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(CategoryImportModel::class.java)
            .with(schema)
            .readValues<CategoryImportModel?>(categoriesCsv)
            .readAll()
            .toList()
    }
}