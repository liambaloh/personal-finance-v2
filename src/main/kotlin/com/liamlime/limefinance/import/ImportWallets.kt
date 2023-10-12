package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.WalletImportModel
import com.liamlime.limefinance.import.util.removeCsvHeaderIfExists


class ImportWallets {
    private val mapper = CsvMapper()
    var schema = mapper.schemaFor(WalletImportModel::class.java);

    private fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun doImport(): List<WalletImportModel> {
        val walletsCsv = readFileFromResources("/wallets.csv")
            .removeCsvHeaderIfExists(schema)
            .joinToString("\n")

        return mapper
            .readerFor(WalletImportModel::class.java)
            .with(schema)
            .readValues<WalletImportModel?>(walletsCsv)
            .readAll()
            .toList()
    }
}