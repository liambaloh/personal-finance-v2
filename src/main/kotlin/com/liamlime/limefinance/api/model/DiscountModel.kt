package com.liamlime.limefinance.api.model

data class DiscountModel(
    override val name: String,
    val currencyAmount: CurrencyAmountModel,
): NameableEntity
