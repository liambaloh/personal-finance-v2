package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.ItemImportModel
import com.liamlime.limefinance.import.model.ReceiptImportModel
import java.io.FileReader
import java.io.Reader

class ImportIn {
    fun readFileFromResources(fileName: String): List<String> {
        return this::class.java.getResource(fileName)
            .readText()
            .lines()
    }

    fun List<String>.removeComments(): List<String> {
        return this.filter { !it.startsWith("#") }
    }

    fun List<String>.removeEmptyLines(): List<String> {
        return this.filter { it.isNotBlank() }
    }

    fun parseLineAsCsv(line: String): List<String> {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
    }

    fun doImport() {
        val lines = readFileFromResources("/in.csv")
            .removeComments()
            .removeEmptyLines()

        var currentReceipt: ReceiptImportModel? = null
        val receipts = lines.map {
            val line = parseLineAsCsv(it)
            when (line.first()) {
                "RECEIPT" -> {
                    val date = line[1]
                    val wallet = line[2]
                    val store = line[3]
                    val receiptCurrency = line[4]
                    val receiptAmount = line[5]
                    val location = line[6]
                    val chargeCurrency = line.getOrNull(7) ?: receiptCurrency
                    val chargeAmount = line.getOrNull(8) ?: receiptAmount
                    currentReceipt = ReceiptImportModel(
                        date = date,
                        wallet = wallet,
                        store = store,
                        receiptCurrency = receiptCurrency,
                        receiptAmount = receiptAmount,
                        location = location,
                        chargeCurrency = chargeCurrency,
                        chargeAmount = chargeAmount,
                    )
                    currentReceipt
                }

                "ITEM" -> {
                    val transactionType = line[1]
                    val categoryName = line[2]
                    val amount = line[3]
                    val count = line[4]
                    val name = line[5]
                    val resolutionName = line[6]
                    val resolutionDatetime = line[7]
                    val locationName = line[8]
                    val tags = line[9]
                    val note = line[10]

                    currentReceipt?.items?.add(ItemImportModel(
                        transactionType = transactionType,
                        category = categoryName,
                        amount = amount,
                        count = count.toInt(),
                        name = name,
                        resolution = resolutionName,
                        resolutionDate = resolutionDatetime,
                        location = locationName,
                        tags = tags.split(" "),
                        note = note
                    ))
                    null
                }

                else -> {
                    null
                }
            }
        }.filterNotNull()

        receipts.forEach {
            println("${it.store} on ${it.date}: ${it.items.count()} items")
        }
    }
}