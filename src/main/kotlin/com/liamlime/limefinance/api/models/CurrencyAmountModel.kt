package com.liamlime.limefinance.api.models

import java.math.BigDecimal

data class CurrencyAmountModel(
    val currency: CurrencyModel,
    val amount: BigDecimal
)
