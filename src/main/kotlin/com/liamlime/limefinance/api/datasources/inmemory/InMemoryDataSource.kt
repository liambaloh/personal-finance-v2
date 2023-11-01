package com.liamlime.limefinance.api.datasources.inmemory

import com.liamlime.limefinance.api.datasources.DataSource
import com.liamlime.limefinance.api.datatypes.ItemAggregationParameter
import com.liamlime.limefinance.api.datatypes.ReceiptAggregationParameter
import com.liamlime.limefinance.api.interfaces.NameableEntity
import com.liamlime.limefinance.api.models.*
import com.liamlime.limefinance.distinctItemAggregatable
import com.liamlime.limefinance.distinctReceiptAggregatable
import com.liamlime.limefinance.import.model.toCurrencyAmountModel
import java.math.BigDecimal

class InMemoryDataSource(
    override val categories: List<CategoryModel>,
    override val currencies: List<CurrencyModel>,
    override val discounts: List<DiscountModel>,
    override val items: List<ItemModel>,
    override val locations: List<LocationModel>,
    override val portfolios: List<PortfolioModel>,
    override val receipts: List<ReceiptModel>,
    override val stores: List<StoreModel>,
    override val tags: List<TagModel>,
    override val wallets: List<WalletModel>,
) : DataSource() {

    override fun toItemSelection(): ItemSelection {
        return ItemSelection(this, items, emptyMap())
    }

    fun itemsByAggregatable(itemAggregationParameter: ItemAggregationParameter): Map<NameableEntity, List<ItemModel>> {
        val aggregatableValues = items.distinctItemAggregatable(itemAggregationParameter)
        return aggregatableValues.associateWith { aggregationValue ->
            items.filter { item ->
                item.getAggregationParameters(itemAggregationParameter)
                    .map { it.name }
                    .contains(aggregationValue.name)
            }
        }
    }

    fun itemsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ItemModel>> {
        val aggregatableValues = receipts.distinctReceiptAggregatable(receiptAggregationParameter)
        return aggregatableValues.associateWith { aggregationValue ->
            receipts
                .filter { it.getAggregationParameter(receiptAggregationParameter).contains(aggregationValue) }
                .flatMap { it.items }
        }
    }

    fun receiptsByAggregatable(receiptAggregationParameter: ReceiptAggregationParameter): Map<NameableEntity, List<ReceiptModel>> {
        val aggregatableValues = receipts.distinctReceiptAggregatable(receiptAggregationParameter)
        return aggregatableValues.associateWith { aggregationValue ->
            receipts
                .filter { it.getAggregationParameter(receiptAggregationParameter).contains(aggregationValue) }
        }
    }

    fun getItemChargeAmount(item: ItemModel): CurrencyAmountModel {
        val itemReceipt = receipts.first { it.items.contains(item) }
        val itemPriceProportionInReceipt =
            if (itemReceipt.receiptCurrencyAmount.amount != BigDecimal.ZERO.setScale(2)) {
                item.currencyAmount.amount / itemReceipt.receiptCurrencyAmount.amount
            } else {
                BigDecimal.ZERO
            }
        return itemReceipt.chargeCurrencyAmount.currency.toCurrencyAmountModel(
            itemPriceProportionInReceipt * itemReceipt.chargeCurrencyAmount.amount
        )
    }
}