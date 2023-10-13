package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.ReceiptAggregationParameter
import com.liamlime.limefinance.api.datatypes.Year
import com.liamlime.limefinance.api.datatypes.sign
import com.liamlime.limefinance.api.interfaces.NameableEntity
import java.time.LocalDateTime

data class ReceiptModel(
    val date: LocalDateTime,
    val wallet: WalletModel,
    val store: StoreModel,
    val receiptCurrencyAmount: CurrencyAmountModel,
    val chargeCurrencyAmount: CurrencyAmountModel,
    val location: LocationModel,
    val note: String,
    val items: List<ItemModel>,
    val discounts: List<DiscountModel>
) {
    fun getAggregationParameter(receiptAggregationParameter: ReceiptAggregationParameter): List<NameableEntity> {
        return when (receiptAggregationParameter) {
            ReceiptAggregationParameter.LOCATION -> listOf(location)
            ReceiptAggregationParameter.STORE -> listOf(store)
            ReceiptAggregationParameter.WALLET -> listOf(wallet)
            ReceiptAggregationParameter.RECEIPT_CURRENCY -> listOf(receiptCurrencyAmount.currency)
            ReceiptAggregationParameter.CHARGE_CURRENCY -> listOf(chargeCurrencyAmount.currency)
            ReceiptAggregationParameter.YEAR -> listOf(Year(date.year.toString()))
            ReceiptAggregationParameter.YEAR_MONTH -> listOf(Year("${date.year}-${date.month}"))
            ReceiptAggregationParameter.YEAR_MONTH_DAY -> listOf(Year("${date.year}-${date.month}-${date.dayOfMonth}"))
            ReceiptAggregationParameter.RECEIPT_AMOUNT_SIGN -> listOf(receiptCurrencyAmount.amount.sign())
            ReceiptAggregationParameter.CHARGE_AMOUNT_SIGN -> listOf(chargeCurrencyAmount.amount.sign())
            ReceiptAggregationParameter.WALLET_TYPE -> listOf(wallet.type)
            ReceiptAggregationParameter.PORTFOLIO -> wallet.portfolios
        }
    }

    fun getAggregationParameters(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
        return items
            .flatMap { it.getAggregationParameters(itemAggregationParameter) }
            .distinct()
    }
}
