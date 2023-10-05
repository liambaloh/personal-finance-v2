package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.ReceiptAggregationParameter
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
        }
    }

    fun getAggregationParameters(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
        return items
            .flatMap { it.getAggregationParameters(itemAggregationParameter) }
            .distinct()
    }
}
