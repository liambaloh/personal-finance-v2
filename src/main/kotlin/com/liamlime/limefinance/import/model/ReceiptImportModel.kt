package com.liamlime.limefinance.import.model

data class ReceiptImportModel(
    val date: String? = null,
    val wallet: String? = null,
    val store: String? = null,
    val receiptCurrency: String? = null,
    val receiptAmount: Double? = null,
    val chargeCurrency: String? = null,
    val chargeAmount: Double? = null,
    val location: String? = null,
    val note: String = "",
    val items: MutableList<ItemImportModel> = mutableListOf(),
    val discounts: MutableList<DiscountImportModel> = mutableListOf()
)
