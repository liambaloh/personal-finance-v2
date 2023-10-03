package com.liamlime.limefinance.api.model

import java.awt.Color
import java.time.LocalDateTime

data class ReceiptModel(
    val date: LocalDateTime,
    val wallet: WalletModel,
    val store: StoreModel,
    val receiptCurrencyAmount: CurrencyAmountModel,
    val chargeCurrencyAmount: CurrencyAmountModel,
    val location: LocationModel,
    val note: String,
    val items: List<ItemModel>
)
