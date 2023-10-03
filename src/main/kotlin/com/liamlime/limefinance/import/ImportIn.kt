package com.liamlime.limefinance.import

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.liamlime.limefinance.import.model.DiscountImportModel
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

    fun List<String>.removeCommands(): List<String> {
        return this.filter { !it.startsWith("~") }
    }

    fun parseLineAsCsv(line: String): List<String> {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
    }

    fun doImport() {
        val lines = readFileFromResources("/in.csv")
            .drop(1)
            .removeComments()
            .removeCommands()
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
                    val receiptAmount = line[5].toDouble()
                    val location = line[6]
                    val chargeCurrency = line.getOrNull(7) ?: receiptCurrency
                    val chargeAmount = line.getOrNull(8)?.toDouble() ?: receiptAmount
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
                    val category = line[2]
                    val amount = line[3].toDouble()
                    val count = line[4].toInt()
                    val name = line[5]
                    val resolutionName = line[6]
                    val resolutionDatetime = line[7]
                    val locationName = line[8]
                    val tags = line[9].split(" ")
                    val note = line[10]

                    currentReceipt?.items?.add(
                        ItemImportModel(
                            transactionType = transactionType,
                            category = category,
                            amount = amount,
                            count = count,
                            name = name,
                            resolution = resolutionName,
                            resolutionDate = resolutionDatetime,
                            location = locationName,
                            tags = tags,
                            note = note
                        )
                    )
                    null
                }

                "DISCOUNT" -> {
                    val name = line[1]
                    val amount = line[2].toDouble()

                    currentReceipt?.discounts?.add(
                        DiscountImportModel(
                            name = name,
                            amount = amount
                        )
                    )
                    null
                }

                else -> {
                    println("LINE FIRST: ${line.first()}")
                    val date = line[0]
                    val wallet = line[1]
                    val transactionType = line[2]
                    val store = line[3]
                    val category = line[4]
                    val currency = line[5]
                    val amount = line[6].toDouble()
                    val count = line[7].toInt()
                    val item = line[8]
                    val resolution = line[9]
                    val resolutionDate = line[10]
                    val locationFrom = line[11]
                    val locationFor = line[12]
                    val tags = line[13].split(" ")
                    val note = line[14]

                    currentReceipt = ReceiptImportModel(
                        date = date,
                        wallet = wallet,
                        store = store,
                        receiptCurrency = currency,
                        receiptAmount = amount,
                        location = locationFrom,
                        chargeCurrency = currency,
                        chargeAmount = amount,
                        items = mutableListOf(
                            ItemImportModel(
                                transactionType = transactionType,
                                category = category,
                                amount = amount,
                                count = count,
                                name = item,
                                resolution = resolution,
                                resolutionDate = resolutionDate,
                                location = locationFor,
                                tags = tags,
                                note = note
                            )
                        )
                    )
                    currentReceipt
                }
            }
        }.filterNotNull()

        receipts.forEach {
            println("${it.store} on ${it.date}: ${it.items.count()} items")
        }
    }
}