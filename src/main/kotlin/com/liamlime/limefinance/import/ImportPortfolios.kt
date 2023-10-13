package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.PortfolioImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportPortfolios {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(PortfolioImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<PortfolioImportModel> {
        val portfoliosCsv = readFileFromResources("/portfolios.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(PortfolioImportModel::class.java)
            .with(schema)
            .readValues<PortfolioImportModel?>(portfoliosCsv)
            .readAll()
            .toList()
    }
}