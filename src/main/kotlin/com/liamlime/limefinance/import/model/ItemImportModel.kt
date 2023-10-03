package com.liamlime.limefinance.import.model

import com.liamlime.limefinance.api.model.*

data class ItemImportModel(
    val transactionType: String? = null,
    val category: String? = null,
    val amount: String? = null,
    val count: Int? = null,
    val name: String? = null,
    val resolution: String? = null,
    val resolutionDate: String? = null,
    val location: String? = null,
    val tags: List<String>? = null,
    val note: String? = null
)
