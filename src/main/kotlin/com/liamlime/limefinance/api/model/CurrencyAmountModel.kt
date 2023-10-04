package com.liamlime.limefinance.api.model

import java.math.BigDecimal

data class CurrencyAmountModel(
    val currency: CurrencyModel,
    val amount: BigDecimal
)
