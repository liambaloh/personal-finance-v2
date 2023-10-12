package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.models.CategoryModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name", "glyph", "backgroundColorHexCode", "textColorHexCode")
data class CategoryImportModel(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("glyph")
    val glyph: String,
    @JsonProperty("backgroundColorHexCode")
    val backgroundColorHexCode: String,
    @JsonProperty("textColorHexCode")
    val textColorHexCode: String
)

fun CategoryImportModel.toCategoryModel(): CategoryModel {
    return CategoryModel(
        name = this.name,
        glyph = this.glyph,
        backgroundColor = backgroundColorHexCode.toColor(),
        textColor = textColorHexCode.toColor()
    )
}