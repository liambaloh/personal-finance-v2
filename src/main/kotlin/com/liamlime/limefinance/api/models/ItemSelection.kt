package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datasources.DataSource
import com.liamlime.limefinance.api.datatypes.*
import com.liamlime.limefinance.api.interfaces.NameableEntity

class ItemSelection(
    private val dataSource: DataSource,
    val items: List<ItemModel>,
    val selectors: Map<AggregationParameter, NameableEntity>
): ItemSelectionOrSelections() {
    fun select(aggregationParameter: AggregationParameter, nameableEntity: NameableEntity): ItemSelection {
        return when (aggregationParameter) {
            is ItemAggregator -> ItemSelection(
                dataSource = dataSource,
                items = filterItemsByItemAggregationParameter(
                    aggregationParameter.aggregationParameter,
                    nameableEntity
                ),
                selectors = selectors + (aggregationParameter to nameableEntity)
            )

            is ReceiptAggregator -> ItemSelection(
                dataSource = dataSource,
                items = filterItemsByReceiptAggregationParameter(
                    aggregationParameter.aggregationParameter,
                    nameableEntity
                ),
                selectors = selectors + (aggregationParameter to nameableEntity)
            )
        }
    }

    private fun filterItemsByItemAggregationParameter(
        aggregationParameter: ItemAggregationParameter,
        nameableEntity: NameableEntity
    ): List<ItemModel> {
        return items.filter {
            it.getAggregationParameters(aggregationParameter)
                .map { it.name }
                .contains(nameableEntity.name)
        }
    }

    private fun filterItemsByReceiptAggregationParameter(
        aggregationParameter: ReceiptAggregationParameter,
        nameableEntity: NameableEntity
    ): List<ItemModel> {
        val itemsInAllReceiptsMatchingAggregation = dataSource.receipts.filter {
            it.getAggregationParameter(aggregationParameter)
                .contains(nameableEntity)
        }.toSet()
        val receiptsOfItemsInSelection = receiptsOfItemsInSelection()

        return receiptsOfItemsInSelection.intersect(itemsInAllReceiptsMatchingAggregation)
            .flatMap { it.items }
    }

    private fun receiptsOfItemsInSelection(): List<ReceiptModel> {
        return dataSource.receipts.filter { it.items.any { items.contains(it) } }
    }

    override fun allItems(): List<ItemModel> = items
}