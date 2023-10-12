package com.liamlime.limefinance.import.model

import com.liamlime.limefinance.api.models.CurrencyModel
import com.liamlime.limefinance.api.models.DiscountModel
import java.math.BigDecimal

data class DiscountImportModel(
    val name: String,
    val currency: String,
    val amount: BigDecimal,
)

fun DiscountImportModel.toDiscountModel(currencies: List<CurrencyModel>): DiscountModel {
    return DiscountModel(
        name = this.name,
        currencyAmount = currencies.first { it.name == this.currency }.toCurrencyAmountModel(this.amount),
    )
}
