package com.liamlime.limefinance

import com.liamlime.limefinance.api.model.*
import com.liamlime.limefinance.import.ImportIn
import com.liamlime.limefinance.import.model.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.math.BigDecimal

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

    val itemsByCategory = receipts.itemsByCategory()
    val itemsByItemCurrency = receipts.itemsByItemCurrency()
    val itemsByReceiptCurrency = receipts.itemsByReceiptCurrency()
    val itemsByReceiptChargeCurrency = receipts.itemsByReceiptChargeCurrency()
    val itemsByItemLocation = receipts.itemsByItemLocation()
    val itemsByReceiptLocation = receipts.itemsByReceiptLocation()
    val itemsByResolution = receipts.itemsByResolution()
    val itemsByTag = receipts.itemsByTag()
    val itemsByWallet = receipts.itemsByWallet()
    val itemsByStore = receipts.itemsByStore()


    val currencyAmountsByCategory = itemsByCategory.aggregateCurrencyAmounts()
    val currencyAmountsByItemCurrency = itemsByItemCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptCurrency = itemsByReceiptCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptChargeCurrency = itemsByReceiptChargeCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByItemLocation = itemsByItemLocation.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptLocation = itemsByReceiptLocation.aggregateCurrencyAmounts()
    val currencyAmountsByResolution = itemsByResolution.aggregateCurrencyAmounts()
    val currencyAmountsByTag = itemsByTag.aggregateCurrencyAmounts()
    val currencyAmountsByWallet = itemsByWallet.aggregateCurrencyAmounts()
    val currencyAmountsByStore = itemsByStore.aggregateCurrencyAmounts()


    currencyAmountsByCategory.printFormatted()

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

fun Map<String, List<CurrencyAmountModel>>.printFormatted() {
    this.forEach { (name, currencyAmountModels) ->
        println("Entity: ${name} has items worth:")
        currencyAmountModels.forEach { currencyAmount ->
            println("\t${currencyAmount.currency.name}: ${currencyAmount.amount}")
        }
    }
}

fun Map<out NameableEntity, List<ItemModel>>.aggregateCurrencyAmounts(): Map<String, List<CurrencyAmountModel>> {
    return this.map { (nameableEntity, items) ->
        val currencyAmounts = items.map { it.currencyAmount }
            .fold(mutableMapOf<CurrencyModel, BigDecimal>()) { currencyAmountsAcc, currencyAmount ->
                currencyAmountsAcc.putIfAbsent(currencyAmount.currency, BigDecimal.ZERO)
                currencyAmountsAcc[currencyAmount.currency] =
                    (currencyAmountsAcc[currencyAmount.currency] ?: 0.toBigDecimal()) + currencyAmount.amount
                currencyAmountsAcc
            }.map { (currency, amount) ->
                CurrencyAmountModel(currency, amount)
            }

        nameableEntity.name to currencyAmounts
    }.toMap()
}

fun List<ReceiptModel>.distinctItemCategories(): List<CategoryModel> {
    return this.flatMap { it.items.map { it.category } }.distinct()
}

fun List<ReceiptModel>.itemsByCategory(): Map<CategoryModel, List<ItemModel>> {
    val categories = distinctItemCategories()
    return categories.associateWith { category ->
        this.flatMap { receipt ->
            receipt.items.filter { item -> item.category == category }
        }
    }
}

fun List<ReceiptModel>.distinctItemCurrencies(): List<CurrencyModel> {
    return this.flatMap { it.items.map { it.currencyAmount.currency } }.distinct()
}

fun List<ReceiptModel>.distinctReceiptCurrencies(): List<CurrencyModel> {
    return this.map { it.receiptCurrencyAmount.currency }.distinct()
}

fun List<ReceiptModel>.distinctReceiptChargeCurrencies(): List<CurrencyModel> {
    return this.map { it.chargeCurrencyAmount.currency }.distinct()
}

fun List<ReceiptModel>.itemsByItemCurrency(): Map<CurrencyModel, List<ItemModel>> {
    val currencies = distinctItemCurrencies()
    return currencies.associateWith { currency ->
        this.flatMap { receipt ->
            receipt.items.filter { item -> item.currencyAmount.currency == currency }
        }
    }
}

fun List<ReceiptModel>.itemsByReceiptCurrency(): Map<CurrencyModel, List<ItemModel>> {
    val currencies = distinctReceiptCurrencies()
    return currencies.associateWith { currency ->
        this
            .filter { it.receiptCurrencyAmount.currency == currency }
            .flatMap { it.items }
    }
}

fun List<ReceiptModel>.itemsByReceiptChargeCurrency(): Map<CurrencyModel, List<ItemModel>> {
    val currencies = distinctReceiptChargeCurrencies()
    return currencies.associateWith { currency ->
        this
            .filter { it.chargeCurrencyAmount.currency == currency }
            .flatMap { it.items }
    }
}

fun List<ReceiptModel>.distinctReceiptLocations(): List<LocationModel> {
    return this.map { it.location }.distinct()
}

fun List<ReceiptModel>.distinctItemLocations(): List<LocationModel> {
    return this.flatMap { it.items.map { it.location } }.distinct()
}

fun List<ReceiptModel>.itemsByItemLocation(): Map<LocationModel, List<ItemModel>> {
    val locations = this.distinctItemLocations()
    return locations.associateWith { location ->
        this.flatMap { receipt ->
            receipt.items.filter { item -> item.location == location }
        }
    }
}

fun List<ReceiptModel>.itemsByReceiptLocation(): Map<LocationModel, List<ItemModel>> {
    val locations = this.distinctReceiptLocations()
    return locations.associateWith { location ->
        this.filter { it.location == location }
            .flatMap { receipt -> receipt.items }
    }
}

fun List<ReceiptModel>.distinctItemResolutions(): List<ResolutionModel> {
    return this.flatMap { it.items.map { it.resolution } }.distinct()
}

fun List<ReceiptModel>.itemsByResolution(): Map<ResolutionModel, List<ItemModel>> {
    val resolutions = this.distinctItemResolutions()
    return resolutions.associateWith { resolution ->
        this.flatMap { receipt ->
            receipt.items.filter { item -> item.resolution == resolution }
        }
    }
}

fun List<ReceiptModel>.distinctItemTags(): List<TagModel> {
    return this.flatMap { it.items.flatMap { it.tags } }.distinct()
}

fun List<ReceiptModel>.itemsByTag(): Map<TagModel, List<ItemModel>> {
    val tags = this.distinctItemTags()
    return tags.associateWith { tag ->
        this.flatMap { receipt ->
            receipt.items.filter { item -> item.tags.contains(tag) }
        }
    }
}

fun List<ReceiptModel>.distinctReceiptWallets(): List<WalletModel> {
    return this.map { it.wallet }.distinct()
}

fun List<ReceiptModel>.itemsByWallet(): Map<WalletModel, List<ItemModel>> {
    val wallets = this.distinctReceiptWallets()
    return wallets.associateWith { wallet ->
        this.filter { it.wallet == wallet }
            .flatMap { receipt -> receipt.items }
    }
}

fun List<ReceiptModel>.distinctReceiptStores(): List<StoreModel> {
    return this.map { it.store }.distinct()
}

fun List<ReceiptModel>.itemsByStore(): Map<StoreModel, List<ItemModel>> {
    val stores = this.distinctReceiptStores()
    return stores.associateWith { store ->
        this.filter { it.store == store }
            .flatMap { receipt -> receipt.items }
    }
}