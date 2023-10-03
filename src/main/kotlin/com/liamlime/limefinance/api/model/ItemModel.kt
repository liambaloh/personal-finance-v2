package com.liamlime.limefinance.api.model

import com.liamlime.limefinance.api.datatype.TransactionType
import java.time.LocalDateTime

data class ItemModel(
    val transactionType: TransactionType,
    val category: CategoryModel,
    val currencyAmount: CurrencyAmountModel,
    val count: Int,
    val name: String,
    val resolution: ResolutionModel,
    val resolutionDate: LocalDateTime,
    val location: LocationModel,
    val tags: List<TagModel>,
    val note: String
)
