package com.liamlime.limefinance.import.model

import com.liamlime.limefinance.api.models.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class ItemImportModel(
    val transactionType: String,
    val category: String,
    val currency: String,
    val amount: BigDecimal,
    val count: Int,
    val name: String,
    val resolution: String,
    val resolutionDate: String,
    val location: String,
    val tags: List<String>,
    val note: String
) {
    fun validateAmountSignCorrespondsToTransactionType(): Boolean {
        return when (transactionType) {
            "income", "reimbursement" -> amount > BigDecimal.ZERO
            "expense" -> amount < BigDecimal.ZERO
            "transfer" -> amount != BigDecimal.ZERO
            else -> false
        }
    }
}

fun List<ItemImportModel>.distinctCategories(): List<String> {
    return this.map { it.category }.distinct()
}

fun List<ItemImportModel>.distinctTags(): List<String> {
    return this.flatMap { it.tags }.distinct()
}

fun List<ItemImportModel>.distinctLocations(): List<String> {
    return this.map { it.location }.distinct()
}

fun List<ItemImportModel>.distinctResolutions(): List<String> {
    return this.map { it.resolution }.distinct()
}

fun ItemImportModel.toItemModel(
    categories: List<CategoryModel>,
    currencies: List<CurrencyModel>,
    resolutions: List<ResolutionModel>,
    locations: List<LocationModel>,
    tags: List<TagModel>
): ItemModel {
    return ItemModel(
        transactionType = this.transactionType.toTransactionType(),
        category = categories.first { it.name == this.category },
        currencyAmount = currencies.first { it.name == this.currency }.toCurrencyAmountModel(this.amount),
        count = this.count,
        name = this.name,
        resolution = resolutions.first { it.name == this.resolution },
        resolutionDate = LocalDateTime.parse(this.resolutionDate, importerDateTimeFormatter),
        location = locations.first { it.name == this.location },
        tags = this.tags.map { tag -> tags.first { it.name == tag } },
        note = this.note
    )
}