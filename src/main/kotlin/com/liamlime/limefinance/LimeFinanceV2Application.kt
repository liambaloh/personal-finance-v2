package com.liamlime.limefinance

import com.liamlime.limefinance.import.ImportIn
import com.liamlime.limefinance.import.model.*
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LimeFinanceV2Application

fun main(args: Array<String>) {
    //runApplication<LimeFinanceV2Application>(*args)
    val receiptImports = ImportIn().doImport()
    val categories = receiptImports.distinctCategories().map { it.toCategoryModel() }
    val tags = receiptImports.distinctTags().map { it.toTagModel() }
    val resolutions = receiptImports.distinctResolutions().map { it.toResolutionModel() }
    val currencies = receiptImports.distinctCurrencies().map { it.toCurrencyModel() }
    val locations = receiptImports.distinctLocations().map { it.toLocationModel() }
    val stores = receiptImports.distinctStores().map { it.toStoreModel() }
    val wallets = receiptImports.distinctWallets().map { it.toWalletModel() }

    println("Looking for mismatches between receipt amount and sum of items and discounts...")
    receiptImports.forEach {
        if (!it.validateReceiptCurrencyAmountWithItemsAndDiscounts()) {
            println("Receipt ${it.store} on ${it.date} failed amount sum verification: Receipt sum: ${it.receiptAmount} " +
                    "Item sum: ${it.items.map { it.amount }.sum()} " +
                    "Discount sum: ${it.discounts.map { it.amount }.sum()}; " +
                    "together: ${it.items.map { it.amount }.sum() + it.discounts.map { it.amount }.sum()} " +
                    "difference: ${
                        it.receiptAmount - (it.items.map { it.amount }.sum() + it.discounts.map { it.amount }.sum())
                    }"
            )
        }
    }

    println("Looking for mismatches between receipt amount and sum of items and discounts...")
    receiptImports.forEach {
        if (!it.validateAmountSignCorrespondsToTransactionType()) {
            println("Receipt ${it.store} on ${it.date} failed amount sign verification: ")
            it.items.forEach { item ->
                if (!item.validateAmountSignCorrespondsToTransactionType()) {
                    println("\tItem ${item.name} failed amount sign verification: Transaction type ${item.transactionType} with amount ${item.amount}")
                }
            }
        }
    }

    val receipts = receiptImports.map {
        it.toReceiptModel(
            wallets = wallets,
            stores = stores,
            currencies = currencies,
            locations = locations,
            categories = categories,
            tags = tags,
            resolutions = resolutions
        )
    }

    //receipts.forEach { receipt ->
    //    println("RECEIPT: ${receipt.store.name} on ${receipt.date} cost ${receipt.receiptCurrencyAmount}")
    //    receipt.items.forEach { item ->
    //        println("\tITEM: ${item.name} cost ${item.currencyAmount}")
    //    }
    //    receipt.discounts.forEach { discount ->
    //        println("\tDISCOUNT: ${discount.name} cost ${discount.currencyAmount}")
    //    }
    //}
}


