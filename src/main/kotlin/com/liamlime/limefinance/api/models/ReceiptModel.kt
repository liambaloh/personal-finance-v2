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
    fun getAggregationParameter(receiptAggregationParameter: ReceiptAggregationParameter): NameableEntity {
        return when (receiptAggregationParameter) {
            ReceiptAggregationParameter.LOCATION -> location
            ReceiptAggregationParameter.STORE -> store
            ReceiptAggregationParameter.WALLET -> wallet
            ReceiptAggregationParameter.RECEIPT_CURRENCY -> receiptCurrencyAmount.currency
            ReceiptAggregationParameter.CHARGE_CURRENCY -> chargeCurrencyAmount.currency
            ReceiptAggregationParameter.YEAR -> Year(date.year.toString())
            ReceiptAggregationParameter.YEAR_MONTH -> Year("${date.year}-${date.month}")
            ReceiptAggregationParameter.YEAR_MONTH_DAY -> Year("${date.year}-${date.month}-${date.dayOfMonth}")
            ReceiptAggregationParameter.RECEIPT_AMOUNT_SIGN -> receiptCurrencyAmount.amount.sign()
            ReceiptAggregationParameter.CHARGE_AMOUNT_SIGN -> chargeCurrencyAmount.amount.sign()
        }
    }

    fun getAggregationParameters(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
        return items
            .flatMap { it.getAggregationParameters(itemAggregationParameter) }
            .distinct()
    }
}
