package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.StoreImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportStores {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(StoreImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<StoreImportModel> {
        val storesCsv = readFileFromResources("/stores.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(StoreImportModel::class.java)
            .with(schema)
            .readValues<StoreImportModel?>(storesCsv)
            .readAll()
            .toList()
    }
}