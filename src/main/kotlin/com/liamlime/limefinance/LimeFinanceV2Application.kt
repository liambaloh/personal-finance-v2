package com.liamlime.limefinance

import com.liamlime.limefinance.api.datasources.inmemory.InMemoryDataSource
import com.liamlime.limefinance.api.datatypes.*
import com.liamlime.limefinance.api.interfaces.NameableEntity
import com.liamlime.limefinance.api.models.*
import com.liamlime.limefinance.import.*
import com.liamlime.limefinance.import.model.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@SpringBootApplication
class LimeFinanceV2Application

val aggregationCommands = mapOf(
    "wallet" to Pair(ReceiptAggregator(ReceiptAggregationParameter.WALLET), arrayOf("wal", "wallet", "wallets")),
    "category" to Pair(ItemAggregator(ItemAggregationParameter.CATEGORY), arrayOf("cat", "category", "categories")),
    "item currency" to Pair(
        ItemAggregator(ItemAggregationParameter.CURRENCY),
        arrayOf("icur", "itemcur", "itemcurrency", "itemcurrencies")
    ),
    "receipt currency" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.RECEIPT_CURRENCY),
        arrayOf("rcur", "receiptcur", "receiptcurrency", "receiptcurrencies")
    ),
    "charge currency" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.CHARGE_CURRENCY),
        arrayOf("ccur", "chargecur", "chargecurrency", "chargecurrencies")
    ),
    "item location" to Pair(
        ItemAggregator(ItemAggregationParameter.LOCATION),
        arrayOf("iloc", "itemloc", "itemlocation", "itemlocations")
    ),
    "receipt location" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.LOCATION),
        arrayOf("rloc", "receiptloc", "receiptlocation", "receiptlocations")
    ),
    "states" to Pair(
        ItemAggregator(ItemAggregationParameter.CURRENT_STATE),
        arrayOf("sta", "state", "states", "csta", "cursta", "currentstate", "currentstates")
    ),
    "existed in state" to Pair(
        ItemAggregator(ItemAggregationParameter.EXISTED_IN_STATE),
        arrayOf("estate", "existedstate", "existedstates", "wasinstate")
    ),
    "tag" to Pair(ItemAggregator(ItemAggregationParameter.TAG), arrayOf("tag", "tags")),
    "store" to Pair(ReceiptAggregator(ReceiptAggregationParameter.STORE), arrayOf("sto", "store", "stores")),
    "transaction type" to Pair(
        ItemAggregator(ItemAggregationParameter.TRANSACTION_TYPE),
        arrayOf("tra", "tratype", "tratypes", "transactiontype", "transactiontypes")
    ),
    "year" to Pair(ReceiptAggregator(ReceiptAggregationParameter.YEAR), arrayOf("yea", "year", "yearss")),
    "year month" to Pair(ReceiptAggregator(ReceiptAggregationParameter.YEAR_MONTH), arrayOf("mon", "month", "months")),
    "year month day" to Pair(ReceiptAggregator(ReceiptAggregationParameter.YEAR_MONTH_DAY), arrayOf("day", "days")),
    "receipt ammount sign" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.RECEIPT_AMOUNT_SIGN),
        arrayOf("ras", "rasign", "rasigns", "receiptammountsign", "receiptammountsigns")
    ),
    "chargea mmount sign" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.CHARGE_AMOUNT_SIGN),
        arrayOf("cas", "casign", "casigns", "chargeammountsign", "chargeammountsigns")
    ),
    "item ammount sign" to Pair(
        ItemAggregator(ItemAggregationParameter.AMOUNT_SIGN),
        arrayOf("ias", "iasign", "itemammountsign", "itemammountsigns")
    ),
    "wallet type" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.WALLET_TYPE),
        arrayOf("wat", "wtype", "wtypes", "wallettype", "wallettypes")
    ),
    "portfolio" to Pair(
        ReceiptAggregator(ReceiptAggregationParameter.PORTFOLIO),
        arrayOf("por", "portfolio", "portfolios")
    ),
)

fun main(args: Array<String>) {
    //runApplication<LimeFinanceV2Application>(*args)
    val receiptImports = ImportIn().doImport()
    val categories = ImportCategories().doImport().map { it.toCategoryModel() }
        .union(receiptImports.distinctCategories().map { it.toCategoryModel() })
        .distinctBy { it.name }
    val locations = ImportLocations().doImport().map { it.toLocationModel() }
        .union(receiptImports.distinctLocations().map { it.toLocationModel() })
        .distinctBy { it.name }
    val resolutions = ImportResolutions().doImport().map { it.toResolutionModel() }
        .union(receiptImports.distinctResolutions().map { it.toResolutionModel() })
        .distinctBy { it.name }
    val stores = ImportStores().doImport().map { it.toStoreModel() }
        .union(receiptImports.distinctStores().map { it.toStoreModel() })
        .distinctBy { it.name }
    val portfoliosFromImport = ImportPortfolios().doImport().map { it.toPortfolioModel() }
    val tags = ImportTags().doImport().map { it.toTagModel() }
        .union(receiptImports.distinctTags().map { it.toTagModel() })
        .distinctBy { it.name }
    val wallets = ImportWallets().doImport().map { it.toWalletModel(portfoliosFromImport) }
        .union(receiptImports.distinctWallets().map { it.toWalletModel() })
        .distinctBy { it.name }
    val currencies = receiptImports.distinctCurrencies().map { it.toCurrencyModel() }

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
        } else {
            acc
        }
    }
    if (!amountSignValidationPassed) {
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

    var currentReceipts = receipts;
    val scan = Scanner(System.`in`)

    var lastAggregationParameter: AggregationParameter? = null
    var lastAggregationValues: List<NameableEntity>? = null

    while (true) {
        println("Enter command: 'options' for options")
        val commandLine = scan.nextLine().trim().lowercase()
        val command = commandLine.split(" ").first()
        val parameters = commandLine
            .split(" ")
            .drop(1)
            .joinToString(" ")
            .split(",")
            .map { it.trim() }
        println("Command: $command; Parameters: $parameters")

        val currentDataSource = InMemoryDataSource(
            categories = categories,
            currencies = currencies,
            discounts = currentReceipts.flatMap { it.discounts }.distinct(),
            items = currentReceipts.flatMap { it.items }.distinct(),
            locations = locations,
            portfolios = portfoliosFromImport,
            receipts = currentReceipts,
            stores = stores,
            tags = tags,
            wallets = wallets,
        )

        when {
            command.startsWith("by") -> {
                val aggregator = aggregationCommands
                    .filter { (_, aggregatorToListOfCommands) ->
                        aggregatorToListOfCommands.second.contains(
                            command.removePrefix("by")
                        )
                    }
                    .map { (_, aggregatorToListOfCommands) -> aggregatorToListOfCommands.first }
                    .firstOrNull()
                lastAggregationParameter = aggregator
                val aggregationParameter = when (aggregator) {
                    is ReceiptAggregator -> aggregator.aggregationParameter
                    is ItemAggregator -> aggregator.aggregationParameter
                    else -> {
                        null
                    }
                }
                val aggregatedItems = when (aggregationParameter) {
                    is ReceiptAggregationParameter -> currentDataSource.itemsByAggregatable(aggregationParameter)
                    is ItemAggregationParameter -> currentDataSource.itemsByAggregatable(aggregationParameter)
                    else -> {
                        println("Unknown aggregation parameter")
                        null
                    }
                }
                if (aggregatedItems != null) {
                    println("Aggregating by $aggregationParameter...")
                    val currencyAmountsByAggregatable = aggregatedItems.aggregateCurrencyAmounts(currentDataSource)
                    lastAggregationValues = currencyAmountsByAggregatable.keys.toList()
                    currencyAmountsByAggregatable.printFormatted()
                }
            }

            command == "filter" -> {
                if(lastAggregationParameter != null) {
                    val receiptAggregationParameter = when (lastAggregationParameter) {
                        is ReceiptAggregator -> lastAggregationParameter.aggregationParameter
                        else -> {
                            null
                        }
                    }
                    val convertedParameters =
                        if(lastAggregationValues != null) {
                            parameters
                                .map { it.toIntOrNull() }
                                .map {
                                    if(it != null) {
                                        lastAggregationValues[it].name.lowercase()
                                    }else{
                                        null
                                    }
                                }.filterNotNull()
                        }
                        else {
                            parameters
                        }
                    currentReceipts = when (receiptAggregationParameter) {
                        is ReceiptAggregationParameter -> {
                            println("Filtering by $receiptAggregationParameter = ${convertedParameters.joinToString(", ")}")
                            currentReceipts
                                .filter { receipt ->
                                    receipt.getAggregationParameter(receiptAggregationParameter)
                                        .map { it.name.lowercase() }
                                        .any { it in convertedParameters }
                                }
                        }

                        else -> {
                            println("Unknown aggregation parameter")
                            currentReceipts
                        }
                    }
                } else {
                    println("No last aggregation parameter")
                }
            }

            command.startsWith("filter") -> {
                val aggregator = aggregationCommands
                    .filter { (_, aggregatorToListOfCommands) ->
                        aggregatorToListOfCommands.second.contains(
                            command.removePrefix(
                                "filter"
                            )
                        )
                    }
                    .map { (_, aggregatorToListOfCommands) -> aggregatorToListOfCommands.first }
                    .firstOrNull()
                val aggregationParameter = when (aggregator) {
                    is ReceiptAggregator -> aggregator.aggregationParameter
                    else -> {
                        null
                    }
                }
                currentReceipts = when (aggregationParameter) {
                    is ReceiptAggregationParameter -> {
                        println("Filtering by $aggregationParameter = ${parameters.joinToString(", ")}")
                        currentReceipts
                            .filter { receipt ->
                                receipt.getAggregationParameter(aggregationParameter)
                                    .map { it.name.lowercase() }
                                    .any { it in parameters }
                            }
                    }

                    else -> {
                        println("Unknown aggregation parameter")
                        currentReceipts
                    }
                }
                lastAggregationParameter = null
            }

            command in listOf("options", "o") -> {
                println("Options: ")
                println(" - by* (e.g. byWal) * is one of:")
                aggregationCommands.forEach { (aggregatorName, aggregatorToListOfCommands) ->
                    val commands = aggregatorToListOfCommands.second
                    println("\t- $aggregatorName = ${commands.joinToString(", ")}")
                }
                println(" - filter* value one, value two, value three (e.g. filterYear 2017, 2018) * is one of:")
                aggregationCommands
                    .filter { (_, aggregatorToListOfCommands) ->
                        aggregatorToListOfCommands.first is ReceiptAggregator
                    }
                    .forEach { (aggregatorName, aggregatorToListOfCommands) ->
                        val commands = aggregatorToListOfCommands.second
                        println("\t- $aggregatorName = ${commands.joinToString(", ")}")
                    }
                println(" - restore (undoes filtering by restoring receipts to all receipts)")
                println(" - options (prints this list)")
                println(" - exit (quits the app)")
            }

            command == "restore" -> {
                currentReceipts = receipts
                lastAggregationParameter = null
            }

            command == "exit" -> {
                break
            }
        }
    }


    val receiptsInThePast = receipts.filter {
        it.date <= LocalDateTime.now()
    }

    val dataSourceWithPastReceipts = InMemoryDataSource(
        categories = categories,
        currencies = currencies,
        discounts = receipts.flatMap { it.discounts }.distinct(),
        items = receipts.flatMap { it.items }.distinct(),
        locations = locations,
        portfolios = portfoliosFromImport,
        receipts = receipts,
        stores = stores,
        tags = tags,
        wallets = wallets,
    )

    val dataSourceWithAllReceipts = InMemoryDataSource(
        categories = categories,
        currencies = currencies,
        discounts = receiptsInThePast.flatMap { it.discounts }.distinct(),
        items = receiptsInThePast.flatMap { it.items }.distinct(),
        locations = locations,
        portfolios = portfoliosFromImport,
        receipts = receiptsInThePast,
        stores = stores,
        tags = tags,
        wallets = wallets,
    )

    val dataSource = dataSourceWithPastReceipts

    receipts
        .receiptsByAggregatable(ReceiptAggregationParameter.YEAR)
        .map { (year, receipts) ->
            year to receipts.sumCharge()
        }.toMap()
        .printFormatted()

    val currencyAmountsByCategoryAndResolution = dataSource
        .itemsByAggregatable(ItemAggregationParameter.CATEGORY)
        .itemsByAggregatable(ItemAggregationParameter.CURRENT_STATE)

    val currencyAmountsByTransactionTypeAndItemAmountSign = dataSource
        .itemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)
        .itemsByAggregatable(ItemAggregationParameter.AMOUNT_SIGN)

    val currencyAmountsByWalletsAndTransactionType = dataSource
        .itemsByAggregatable(ReceiptAggregationParameter.WALLET)
        .itemsByAggregatable(ItemAggregationParameter.TRANSACTION_TYPE)

    currencyAmountsByCategoryAndResolution.forEach { (category, itemsInCategoryByResolution) ->
        println("Category: ${category.name} has items by resolution:")
        val currencyAmountsInCategoryByResolution = itemsInCategoryByResolution.aggregateCurrencyAmounts(dataSource)
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


    currencyAmountsByWalletsAndTransactionType
        .print1(0, dataSource)
}

fun Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>>>.print3(
    indentation: Int = 0, dataSource: InMemoryDataSource
) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        itemsByInnerNameableEntity.print2(indentation + 1, dataSource)
    }
}

fun Map<NameableEntity, Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>>.print2(
    indentation: Int = 0,
    dataSource: InMemoryDataSource
) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        itemsByInnerNameableEntity.print1(indentation + 1, dataSource)
    }
}

fun Map<NameableEntity, Map<NameableEntity, List<ItemModel>>>.print1(
    indentation: Int = 0,
    dataSource: InMemoryDataSource
) {
    this.forEach { (outerNameableEntity, itemsByInnerNameableEntity) ->
        println("${"\t".repeat(indentation)}Entity: ${outerNameableEntity.name} has items:")
        val currencyAmountsInCategoryByResolution = itemsByInnerNameableEntity.aggregateCurrencyAmounts(dataSource)
        currencyAmountsInCategoryByResolution.printFormatted(indentation + 1)
    }
}

fun Map<NameableEntity, List<CurrencyAmountModel>>.printFormatted(indentation: Int = 0) {
    this.onEachIndexed { index, (nameableEntity, currencyAmountModels) ->
        println("${"\t".repeat(indentation)}$index: Entity: ${nameableEntity.name} has items worth:")
        currencyAmountModels.forEach { currencyAmount ->
            println("${"\t".repeat(indentation + 1)}${currencyAmount.currency.name}: ${currencyAmount.amount}")
        }
    }
}

fun Map<out NameableEntity, List<ItemModel>>.aggregateCurrencyAmounts(dataSource: InMemoryDataSource): Map<NameableEntity, List<CurrencyAmountModel>> {
    return this.map { (nameableEntity, items) ->
        val currencyAmounts = items.map { dataSource.getItemChargeAmount(it) }
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
            .filter { it.getAggregationParameter(receiptAggregationParameter).contains(aggregationValue) }
            .flatMap { it.items }
    }
}

fun Collection<ReceiptModel>.receiptsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ReceiptModel>> {
    val aggregatableValues = this.distinctReceiptAggregatable(receiptAggregationParameter)
    return aggregatableValues.associateWith { aggregationValue ->
        this
            .filter { it.getAggregationParameter(receiptAggregationParameter).contains(aggregationValue) }
    }
}


fun Collection<ItemModel>.distinctItemAggregatable(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
    return this.flatMap { it.getAggregationParameters(itemAggregationParameter) }.distinct()
}

fun Collection<ReceiptModel>.distinctReceiptItemAggregatable(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
    return this.flatMap { it.getAggregationParameters(itemAggregationParameter) }.distinct()
}

fun Collection<ReceiptModel>.distinctReceiptAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): List<NameableEntity> {
    return this.flatMap { it.getAggregationParameter(receiptAggregationParameter) }.distinct()
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