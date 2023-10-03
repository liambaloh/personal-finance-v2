package com.liamlime.limefinance.import.model

import com.liamlime.limefinance.api.model.*
import java.time.LocalDateTime

data class ReceiptImportModel(
    val date: String? = null,
    val wallet: String? = null,
    val store: String? = null,
    val receiptCurrency: String? = null,
    val receiptAmount: String? = null,
    val chargeCurrency: String? = null,
    val chargeAmount: String? = null,
    val location: String? = null,
    val note: String = "",
    val items: MutableList<ItemImportModel> = mutableListOf()
)
