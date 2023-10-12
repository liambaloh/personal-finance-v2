package com.liamlime.limefinance.import.model

import com.liamlime.limefinance.api.datatypes.TransactionType
import com.liamlime.limefinance.api.datatypes.WalletType
import com.liamlime.limefinance.api.models.*
import java.awt.Color
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val importerDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

data class ReceiptImportModel(
    val date: String,
    val wallet: String,
    val store: String,
    val receiptCurrency: String,
    val receiptAmount: BigDecimal,
    val chargeCurrency: String,
    val chargeAmount: BigDecimal,
    val location: String,
    val note: String = "",
    val items: MutableList<ItemImportModel> = mutableListOf(),
    val discounts: MutableList<DiscountImportModel> = mutableListOf()
) {
    fun validateReceiptCurrencyAmountWithItemsAndDiscounts(): Boolean {
        val receiptCurrencyAmount = this.receiptAmount
        val itemsCurrencyAmount = this.items.map { it.amount }.sum()
        val discountsCurrencyAmount = this.discounts.map { it.amount }.sum()
        return receiptCurrencyAmount == itemsCurrencyAmount + discountsCurrencyAmount
    }

    fun validateAmountSignCorrespondsToTransactionType(): Boolean {
        return items.map { it.validateAmountSignCorrespondsToTransactionType() }.all { it }
    }
}

fun List<BigDecimal>.sum(): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, bigDecimal -> acc + bigDecimal }
}

fun List<ReceiptImportModel>.validateReciprocalTransfer() {
    this
        .flatMap { receipt -> receipt.items.map { item -> Pair(receipt, item) } }
        .filter { it.second.transactionType.uppercase().trim() == "TRANSFER" }
        .fold(mutableListOf<Pair<ReceiptImportModel, ItemImportModel>>()){ acc, (receipt, item) ->
            val found = acc.firstOrNull { it.first.date == receipt.date && it.second.amount == -item.amount }
            if(found != null){
                acc.remove(found)
            }else{
                acc.add(Pair(receipt, item))
            }
            acc
        }
        .forEach { (receipt, item) -> println("TRANSFER ON ${receipt.date}: ${item.amount}") }
}

fun List<ReceiptImportModel>.distinctWallets(): List<String> {
    return this.map { it.wallet }.distinct()
}

fun List<ReceiptImportModel>.distinctStores(): List<String> {
    return this.map { it.store }.distinct()
}

fun List<ReceiptImportModel>.distinctLocations(): List<String> {
    return this.map { it.location }
        .union(this.map { it.items.distinctLocations() }.flatten())
        .distinct()
        .toList()
}

fun List<ReceiptImportModel>.distinctCurrencies(): List<String> {
    return this.map { it.receiptCurrency }
        .union(this.map { it.chargeCurrency })
        .distinct()
}

fun List<ReceiptImportModel>.distinctCategories(): List<String> {
    return this.map { it.items.distinctCategories() }.flatten().distinct()
}

fun List<ReceiptImportModel>.distinctTags(): List<String> {
    return this.map { it.items.distinctTags() }.flatten().distinct()
}

fun List<ReceiptImportModel>.distinctResolutions(): List<String> {
    return this.map { it.items.distinctResolutions() }.flatten().distinct()
}

fun randomForegroundColor(): Color {
    return Color(
        ((8..14).random() * 8),
        ((8..14).random() * 8),
        ((8..14).random() * 8)
    )
}

fun randomBackgroundColor(): Color {
    return Color(
        ((18..24).random() * 8),
        ((18..24).random() * 8),
        ((18..24).random() * 8)
    )
}

fun String.toCategoryModel(): CategoryModel {
    return CategoryModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor()
    )
}

fun CurrencyModel.toCurrencyAmountModel(amount: BigDecimal): CurrencyAmountModel {
    return CurrencyAmountModel(
        currency = this,
        amount = amount
    )
}

fun String.toCurrencyModel(): CurrencyModel {
    return CurrencyModel(
        name = this
    )
}

fun String.toLocationModel(): LocationModel {
    return LocationModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor()
    )
}

fun String.toResolutionModel(): ResolutionModel {
    return ResolutionModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor(),
        utility = 0.0,
        currentlyOwned = false,
        expected = false
    )
}

fun String.toStoreModel(): StoreModel {
    return StoreModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor()
    )
}

fun String.toTagModel(): TagModel {
    return TagModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor()
    )
}

fun String.toWalletModel(): WalletModel {
    return WalletModel(
        name = this,
        glyph = "",
        textColor = randomForegroundColor(),
        backgroundColor = randomBackgroundColor(),
        type = WalletType.UNKNOWN
    )
}

fun String.toTransactionType(): TransactionType {
    return when (this.trim().uppercase()) {
        "EXPENSE" -> TransactionType.EXPENSE
        "INCOME" -> TransactionType.INCOME
        "TRANSFER" -> TransactionType.TRANSFER
        "REIMBURSEMENT" -> TransactionType.REIMBURSEMENT
        "CAPITALGAIN" -> TransactionType.CAPITAL_GAIN
        "CAPITALLOSS" -> TransactionType.CAPITAL_LOSS
        "INVESTMENT" -> TransactionType.INVESTMENT
        "DIVESTMENT" -> TransactionType.DIVESTMENT
        else -> TransactionType.UNKNOWN
    }
}

fun ReceiptImportModel.toReceiptModel(
    wallets: List<WalletModel>,
    stores: List<StoreModel>,
    currencies: List<CurrencyModel>,
    locations: List<LocationModel>,
    tags: List<TagModel>,
    categories: List<CategoryModel>,
    resolutions: List<ResolutionModel>,
): ReceiptModel {
    return ReceiptModel(
        date = LocalDateTime.parse(this.date, importerDateTimeFormatter),
        wallet = wallets.first { it.name == this.wallet },
        store = stores.first { it.name == this.store },
        receiptCurrencyAmount = currencies.first { it.name == this.receiptCurrency }
            .toCurrencyAmountModel(this.receiptAmount),
        chargeCurrencyAmount = currencies.first { it.name == this.chargeCurrency }
            .toCurrencyAmountModel(this.chargeAmount),
        location = locations.first { it.name == this.location },
        note = this.note,
        items = this.items.map { item -> item.toItemModel(categories, currencies, resolutions, locations, tags) },
        discounts = this.discounts.map { discount -> discount.toDiscountModel(currencies) }
    )
}

fun String.toAmount(): BigDecimal {
    return this.toBigDecimal().setScale(2)
}