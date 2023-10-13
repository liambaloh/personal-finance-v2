package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.models.PortfolioModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name")
data class PortfolioImportModel(
    @JsonProperty("name")
    val name: String,
)

fun PortfolioImportModel.toPortfolioModel(): PortfolioModel {
    return PortfolioModel(
        name = this.name,
        glyph = "unknown",
        backgroundColor = "FFFFFF".toColor(),
        textColor = "000000".toColor()
    )
}