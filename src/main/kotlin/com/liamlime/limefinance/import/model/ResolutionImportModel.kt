package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.models.ResolutionModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name", "backgroundColorHexCode", "textColorHexCode", "utility", "currentlyOwned", "expected")
data class ResolutionImportModel(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("backgroundColorHexCode")
    val backgroundColorHexCode: String,

    @JsonProperty("textColorHexCode")
    val textColorHexCode: String,

    @JsonProperty("utility")
    val utility: Double,

    @JsonProperty("currentlyOwned")
    val currentlyOwned: Boolean,

    @JsonProperty("expected")
    val expected: Boolean
)

fun ResolutionImportModel.toResolutionModel(): ResolutionModel {
    return ResolutionModel(
        name = this.name,
        glyph = "unknown",
        backgroundColor = backgroundColorHexCode.toColor(),
        textColor = textColorHexCode.toColor(),
        utility = this.utility,
        currentlyOwned = this.currentlyOwned,
        expected = this.expected
    )
}