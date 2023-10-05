package com.liamlime.limefinance

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.ReceiptAggregationParameter
import com.liamlime.limefinance.api.interfaces.NameableEntity
import com.liamlime.limefinance.api.models.*
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

    val itemsByCategory = receipts.allItemsByAggregatable(ItemAggregationParameter.CATEGORY)
    val itemsByItemCurrency = receipts.allItemsByAggregatable(ItemAggregationParameter.CURRENCY)
    val itemsByReceiptCurrency = receipts.allItemsByAggregatable(ReceiptAggregationParameter.RECEIPT_CURRENCY)
    val itemsByReceiptChargeCurrency = receipts.allItemsByAggregatable(ReceiptAggregationParameter.CHARGE_CURRENCY)
    val itemsByItemLocation = receipts.allItemsByAggregatable(ItemAggregationParameter.LOCATION)
    val itemsByReceiptLocation = receipts.allItemsByAggregatable(ReceiptAggregationParameter.LOCATION)
    val itemsByResolution = receipts.allItemsByAggregatable(ItemAggregationParameter.RESOLUTION)
    val itemsByTag = receipts.allItemsByAggregatable(ItemAggregationParameter.TAG)
    val itemsByWallet = receipts.allItemsByAggregatable(ReceiptAggregationParameter.WALLET)
    val itemsByStore = receipts.allItemsByAggregatable(ReceiptAggregationParameter.STORE)
    val itemsByTransactionType = receipts.allItemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)


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
    val currencyAmountsByTransactionType = itemsByTransactionType.aggregateCurrencyAmounts()


    val currencyAmountsByCategoryAndResolution = receipts
        .allItemsByAggregatable(ItemAggregationParameter.CATEGORY)
        .itemsByAggregatable(ItemAggregationParameter.RESOLUTION)

    currencyAmountsByCategoryAndResolution.forEach { (category, itemsInCategoryByResolution) ->
        println("Category: ${category.name} has items by resolution:")
        val currencyAmountsInCategoryByResolution = itemsInCategoryByResolution.aggregateCurrencyAmounts()
        currencyAmountsInCategoryByResolution.printFormatted()
    }


    //currencyAmountsByStore.printFormatted()

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

fun Collection<ReceiptModel>.allItems(): List<ItemModel> {
    return this.flatMap { it.items }
}

fun Collection<ReceiptModel>.allItemsByAggregatable(itemAggregationParameter: ItemAggregationParameter): Map<NameableEntity, List<ItemModel>> {
    return this.allItems().itemsByAggregatable(itemAggregationParameter)
}

fun Map<NameableEntity, List<ItemModel>>.itemsByAggregatable(itemAggregationParameter: ItemAggregationParameter): Map<NameableEntity, Map<NameableEntity, List<ItemModel>>> {
    return this.keys.associateWith { parentNameableEntity ->
        val itemsForThisNameableEntity = this[parentNameableEntity] ?: emptyList()
        val aggregatableValues = itemsForThisNameableEntity.distinctItemAggregatable(itemAggregationParameter)
        aggregatableValues.associateWith { aggregationValue ->
            itemsForThisNameableEntity.filter { item -> item.getAggregationParameters(itemAggregationParameter).contains(aggregationValue) }
        }
    }
}

fun Collection<ItemModel>.itemsByAggregatable(itemAggregationParameter: ItemAggregationParameter): Map<NameableEntity, List<ItemModel>> {
    val aggregatableValues = this.distinctItemAggregatable(itemAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this.filter { item -> item.getAggregationParameters(itemAggregationParameter).contains(aggregationValue) }
    }
}

fun Collection<ReceiptModel>.allItemsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ItemModel>> {
    val aggregatableValues = this.distinctReceiptAggregatable(receiptAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this
            .filter { it.getAggregationParameter(receiptAggregationParameter) == aggregationValue}
            .flatMap { it.items }
    }
}




fun Collection<ItemModel>.distinctItemAggregatable(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
    return this.flatMap { it.getAggregationParameters(itemAggregationParameter) }.distinct()
}

fun Collection<ReceiptModel>.distinctReceiptItemAggregatable(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
    return this.flatMap { it.getAggregationParameters(itemAggregationParameter) }.distinct()
}

fun Collection<ReceiptModel>.distinctReceiptAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): List<NameableEntity> {
    return this.map { it.getAggregationParameter(receiptAggregationParameter) }.distinct()
}