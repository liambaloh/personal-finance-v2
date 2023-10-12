package com.liamlime.limefinance

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.ReceiptAggregationParameter
import com.liamlime.limefinance.api.interfaces.NameableEntity
import com.liamlime.limefinance.api.models.*
import com.liamlime.limefinance.import.*
import com.liamlime.limefinance.import.model.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.math.BigDecimal

@SpringBootApplication
class LimeFinanceV2Application

fun main(args: Array<String>) {
    //runApplication<LimeFinanceV2Application>(*args)
    val categoryImports = ImportCategories().doImport()
    val locationImports = ImportLocations().doImport()
    val resolutionImports = ImportResolutions().doImport()
    val storeImports = ImportStores().doImport()
    val tagImports = ImportTags().doImport()
    val walletImports = ImportWallets().doImport()
    val categoriesFromImport = categoryImports.map { it.toCategoryModel() }
    val locationsFromImport = locationImports.map { it.toLocationModel() }
    val resolutionsFromImport = resolutionImports.map { it.toResolutionModel() }
    val storesFromImport = storeImports.map { it.toStoreModel() }
    val tagsFromImport = tagImports.map { it.toTagModel() }
    val walletsFromImport = walletImports.map { it.toWalletModel() }


    val receiptImports = ImportIn().doImport()
    val categoriesFromReceipts = receiptImports.distinctCategories().map { it.toCategoryModel() }
    val tagsFromReceipts = receiptImports.distinctTags().map { it.toTagModel() }
    val resolutionsFromReceipts = receiptImports.distinctResolutions().map { it.toResolutionModel() }
    val currenciesFromReceipts = receiptImports.distinctCurrencies().map { it.toCurrencyModel() }
    val locationsFromReceipts = receiptImports.distinctLocations().map { it.toLocationModel() }
    val storesFromReceipts = receiptImports.distinctStores().map { it.toStoreModel() }
    val walletsFromReceipts = receiptImports.distinctWallets().map { it.toWalletModel() }

    val categories = categoriesFromImport
        .union(categoriesFromReceipts)
        .distinctBy { it.name }
    val wallets = walletsFromImport
        .union(walletsFromReceipts)
        .distinctBy { it.name }
    val stores = storesFromImport
        .union(storesFromReceipts)
        .distinctBy { it.name }
    val locations = locationsFromImport
        .union(locationsFromReceipts)
        .distinctBy { it.name }
    val tags = tagsFromImport
        .union(tagsFromReceipts)
        .distinctBy { it.name }
    val resolutions = resolutionsFromImport
        .union(resolutionsFromReceipts)
        .distinctBy { it.name }
    val currencies = currenciesFromReceipts

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

    println("Looking for mismatches between amount signs and transaction types (e.g. negative income or positive expense)...")
    val amountSignValidationPassed = receiptImports.fold(true) { acc, it ->
        if (!it.validateAmountSignCorrespondsToTransactionType()) {
            println("Receipt ${it.store} on ${it.date} failed amount sign verification: ")
            it.items.forEach { item ->
                if (!item.validateAmountSignCorrespondsToTransactionType()) {
                    println("\tItem ${item.name} failed amount sign verification: Transaction type ${item.transactionType} with amount ${item.amount}")
                }
            }
            false
        }else {
            acc
        }
    }
    if(!amountSignValidationPassed){
        throw Exception("Some receipts failed amount sign verification")
    }
    receiptImports.validateReciprocalTransfer()

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
    val itemsByCurrentState = receipts.allItemsByAggregatable(ItemAggregationParameter.CURRENT_STATE)
    val itemsByStatesTheyExistedIn = receipts.allItemsByAggregatable(ItemAggregationParameter.EXISTED_IN_STATE)
    val itemsByTag = receipts.allItemsByAggregatable(ItemAggregationParameter.TAG)
    val itemsByWallet = receipts.allItemsByAggregatable(ReceiptAggregationParameter.WALLET)
    val itemsByStore = receipts.allItemsByAggregatable(ReceiptAggregationParameter.STORE)
    val itemsByTransactionType = receipts.allItemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)
    val itemsByYear = receipts.allItemsByAggregatable(ReceiptAggregationParameter.YEAR)
    val itemsByYearMonth = receipts.allItemsByAggregatable(ReceiptAggregationParameter.YEAR_MONTH)
    val itemsByYearMonthDay = receipts.allItemsByAggregatable(ReceiptAggregationParameter.YEAR_MONTH_DAY)
    val itemsByReceiptAmountSign = receipts.allItemsByAggregatable(ReceiptAggregationParameter.RECEIPT_AMOUNT_SIGN)
    val itemsByChargeAmountSign = receipts.allItemsByAggregatable(ReceiptAggregationParameter.CHARGE_AMOUNT_SIGN)
    val itemsByItemAmountSign = receipts.allItemsByAggregatable(ItemAggregationParameter.AMOUNT_SIGN)
    val itemsByWalletType = receipts.allItemsByAggregatable(ReceiptAggregationParameter.WALLET_TYPE)

    receipts
        .receiptsByAggregatable(ReceiptAggregationParameter.YEAR)
        .map { (year, receipts) ->
            year to receipts.sumCharge()
        }.toMap()
        .printFormatted()

    val currencyAmountsByCategory = itemsByCategory.aggregateCurrencyAmounts()
    val currencyAmountsByItemCurrency = itemsByItemCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptCurrency = itemsByReceiptCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptChargeCurrency = itemsByReceiptChargeCurrency.aggregateCurrencyAmounts()
    val currencyAmountsByItemLocation = itemsByItemLocation.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptLocation = itemsByReceiptLocation.aggregateCurrencyAmounts()
    val currencyAmountsByCurrentState = itemsByCurrentState.aggregateCurrencyAmounts()
    val currencyAmountsByExistedInStates = itemsByStatesTheyExistedIn.aggregateCurrencyAmounts()
    val currencyAmountsByTag = itemsByTag.aggregateCurrencyAmounts()
    val currencyAmountsByWallet = itemsByWallet.aggregateCurrencyAmounts()
    val currencyAmountsByStore = itemsByStore.aggregateCurrencyAmounts()
    val currencyAmountsByTransactionType = itemsByTransactionType.aggregateCurrencyAmounts()
    val currencyAmountsByYear = itemsByYear.aggregateCurrencyAmounts()
    val currencyAmountsByYearMonth = itemsByYearMonth.aggregateCurrencyAmounts()
    val currencyAmountsByYearMonthDay = itemsByYearMonthDay.aggregateCurrencyAmounts()
    val currencyAmountsByReceiptAmountSign = itemsByReceiptAmountSign.aggregateCurrencyAmounts()
    val currencyAmountsByChargeAmountSign = itemsByChargeAmountSign.aggregateCurrencyAmounts()
    val currencyAmountsByItemAmountSign = itemsByItemAmountSign.aggregateCurrencyAmounts()
    val currencyAmountsByWalletType = itemsByWalletType.aggregateCurrencyAmounts()

    val currencyAmountsByCategoryAndResolution = receipts
        .allItemsByAggregatable(ItemAggregationParameter.CATEGORY)
        .itemsByAggregatable(ItemAggregationParameter.CURRENT_STATE)

    val currencyAmountsByTransactionTypeAndItemAmountSign = receipts
        .allItemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)
        .itemsByAggregatable(ItemAggregationParameter.AMOUNT_SIGN)

    val currencyAmountsByWalletsAndTransactionType = receipts
        .allItemsByAggregatable(ReceiptAggregationParameter.WALLET)
        .itemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)

    currencyAmountsByCategoryAndResolution.forEach { (category, itemsInCategoryByResolution) ->
        println("Category: ${category.name} has items by resolution:")
        val currencyAmountsInCategoryByResolution = itemsInCategoryByResolution.aggregateCurrencyAmounts()
        currencyAmountsInCategoryByResolution.printFormatted()
    }

    receipts.forEach { receipt ->
        println("RECEIPT: ${receipt.store.name} on ${receipt.date} cost ${receipt.receiptCurrencyAmount}")
        receipt.items.forEach { item ->
            println("\tITEM: ${item.name} cost ${item.currencyAmount}")
        }
        receipt.discounts.forEach { discount ->
            println("\tDISCOUNT: ${discount.name} cost ${discount.currencyAmount}")
        }
    }


    currencyAmountsByWalletType.printFormatted()

    currencyAmountsByWalletsAndTransactionType
        .print1()
}

fun Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>>>.print3(
    indentation: Int = 0
) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        itemsByInnerNameableEntity.print2(indentation + 1)
    }
}

fun Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>>.print2(indentation: Int = 0) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        itemsByInnerNameableEntity.print1(indentation + 1)
    }
}

fun Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>.print1(indentation: Int = 0) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        val currencyAmountsInCategoryByResolution = itemsByInnerNameableEntity.aggregateCurrencyAmounts()
        currencyAmountsInCategoryByResolution.printFormatted(indentation + 1)
    }
}

fun Map<NameableEntity, List<CurrencyAmountModel>>.printFormatted(indentation: Int = 0) {
    this.forEach { (nameableEntity, currencyAmountModels) ->
        println("${"\t".repeat(indentation)}Entity: ${nameableEntity.name} has items worth:")
        currencyAmountModels.forEach { currencyAmount ->
            println("${"\t".repeat(indentation + 1)}${currencyAmount.currency.name}: ${currencyAmount.amount}")
        }
    }
}

fun Map<out NameableEntity, List<ItemModel>>.aggregateCurrencyAmounts(): Map<NameableEntity, List<CurrencyAmountModel>> {
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

        nameableEntity to currencyAmounts
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
            itemsForThisNameableEntity
                .filter { item ->
                    item.getAggregationParameters(itemAggregationParameter)
                        .map { it.name }
                        .contains(aggregationValue.name)
                }
        }
    }
}

fun Collection<ItemModel>.itemsByAggregatable(itemAggregationParameter: ItemAggregationParameter): Map<NameableEntity, List<ItemModel>> {
    val aggregatableValues = this.distinctItemAggregatable(itemAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this.filter { item ->
            item.getAggregationParameters(itemAggregationParameter)
                .map { it.name }
                .contains(aggregationValue.name)
        }
    }
}

fun Collection<ReceiptModel>.allItemsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ItemModel>> {
    val aggregatableValues = this.distinctReceiptAggregatable(receiptAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this
            .filter { it.getAggregationParameter(receiptAggregationParameter).name == aggregationValue.name }
            .flatMap { it.items }
    }
}

fun Collection<ReceiptModel>.receiptsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ReceiptModel>> {
    val aggregatableValues = this.distinctReceiptAggregatable(receiptAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this
            .filter { it.getAggregationParameter(receiptAggregationParameter).name == aggregationValue.name }
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

fun Collection<ReceiptModel>.sumCharge(): List<CurrencyAmountModel> {
    return this
        .groupBy { it.chargeCurrencyAmount.currency }
        .map { (currency, receipts) ->
            CurrencyAmountModel(
                currency = currency,
                amount = receipts.sumOf { it.chargeCurrencyAmount.amount }
            )
        }
}