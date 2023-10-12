package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.TransactionType
import com.liamlime.limefinance.api.datatypes.sign
import com.liamlime.limefinance.api.interfaces.NameableEntity
import com.liamlime.limefinance.import.model.toCurrencyAmountModel
import java.time.LocalDateTime

data class ItemModel(
    val transactionType: TransactionType,
    val category: CategoryModel,
    val currencyAmount: CurrencyAmountModel,
    val count: Int,
    override val name: String,
    val states: List<ItemState>,
    val location: LocationModel,
    val tags: List<TagModel>,
    val note: String
) : NameableEntity {
    val currentState: ResolutionModel = states.last().resolution

    fun getAggregationParameters(itemAggregationParameter: ItemAggregationParameter): List<NameableEntity> {
        return when (itemAggregationParameter) {
            ItemAggregationParameter.CATEGORY -> listOf(category)
            ItemAggregationParameter.LOCATION -> listOf(location)
            ItemAggregationParameter.CURRENCY -> listOf(currencyAmount.currency)
            ItemAggregationParameter.CURRENT_STATE -> listOf(states.last().resolution)
            ItemAggregationParameter.EXISTED_IN_STATE -> states.map { it.resolution }
            ItemAggregationParameter.TAG -> tags
            ItemAggregationParameter.TRANSACTION_TYPE -> listOf(transactionType)
            ItemAggregationParameter.AMOUNT_SIGN -> listOf(currencyAmount.amount.sign())
        }
    }

    fun utility(): CurrencyAmountModel {
        return currencyAmount.currency.toCurrencyAmountModel(
            currentState.utility.toBigDecimal() * currencyAmount.amount
        )
    }

    fun utilityPerDay(): CurrencyAmountModel {
        return currencyAmount.currency.toCurrencyAmountModel(
            currentState.utility.toBigDecimal() * currencyAmount.amount
        )
    }
}

