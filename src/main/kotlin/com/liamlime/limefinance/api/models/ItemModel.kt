package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.TransactionType
import com.liamlime.limefinance.api.interfaces.NameableEntity
import java.time.LocalDateTime

data class ItemModel(
    val transactionType: TransactionType,
    val category: CategoryModel,
    val currencyAmount: CurrencyAmountModel,
    val count: Int,
    override val name: String,
    val resolution: ResolutionModel,
    val resolutionDate: LocalDateTime,
    val location: LocationModel,
    val tags: List<TagModel>,
    val note: String
) : NameableEntity {
    fun getAggregationParameters(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
        return when (itemAggregationParameter) {
            ItemAggregationParameter.CATEGORY -> listOf(category)
            ItemAggregationParameter.LOCATION -> listOf(location)
            ItemAggregationParameter.CURRENCY -> listOf(currencyAmount.currency)
            ItemAggregationParameter.RESOLUTION -> listOf(resolution)
            ItemAggregationParameter.TAG -> tags
            ItemAggregationParameter.TRANSACTION_TYPE -> listOf(transactionType)
        }
    }
}

