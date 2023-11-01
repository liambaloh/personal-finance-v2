package com.liamlime.limefinance.api.datasources

import com.liamlime.limefinance.api.models.*

abstract class DataSource {
    abstract val categories: List<CategoryModel>
    abstract val currencies: List<CurrencyModel>
    abstract val discounts: List<DiscountModel>
    abstract val items: List<ItemModel>
    abstract val locations: List<LocationModel>
    abstract val portfolios: List<PortfolioModel>
    abstract val receipts: List<ReceiptModel>
    abstract val stores: List<StoreModel>
    abstract val tags: List<TagModel>
    abstract val wallets: List<WalletModel>

    abstract fun toItemSelection(): ItemSelection
}