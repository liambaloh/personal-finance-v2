package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.ResolutionImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportResolutions {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(ResolutionImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<ResolutionImportModel> {
        val resolutionsCsv = readFileFromResources("/resolutions.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(ResolutionImportModel::class.java)
            .with(schema)
            .readValues<ResolutionImportModel?>(resolutionsCsv)
            .readAll()
            .toList()
    }
}