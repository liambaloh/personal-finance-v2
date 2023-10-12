package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.TagImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportTags {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(TagImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<TagImportModel> {
        val tagsCsv = readFileFromResources("/tags.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(TagImportModel::class.java)
            .with(schema)
            .readValues<TagImportModel?>(tagsCsv)
            .readAll()
            .toList()
    }
}