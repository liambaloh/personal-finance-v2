package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.interfaces.NameableEntity

data class DiscountModel(
    override val name: String,
    val currencyAmount: CurrencyAmountModel,
): NameableEntity
