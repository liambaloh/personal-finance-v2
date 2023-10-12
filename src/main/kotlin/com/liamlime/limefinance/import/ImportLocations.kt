package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.LocationImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportLocations {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(LocationImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<LocationImportModel> {
        val locationsCsv = readFileFromResources("/locations.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(LocationImportModel::class.java)
            .with(schema)
            .readValues<LocationImportModel?>(locationsCsv)
            .readAll()
            .toList()
    }
}