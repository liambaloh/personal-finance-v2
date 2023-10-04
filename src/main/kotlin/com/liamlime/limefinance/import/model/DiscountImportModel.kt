package com.liamlime.limefinance.import.model

import java.math.BigDecimal

data class DiscountImportModel(
    val name: String,
    val currency: String,
    val amount: BigDecimal,
)
