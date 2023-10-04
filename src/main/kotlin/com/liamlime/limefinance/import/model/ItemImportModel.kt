package com.liamlime.limefinance.import.model

import java.math.BigDecimal

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
