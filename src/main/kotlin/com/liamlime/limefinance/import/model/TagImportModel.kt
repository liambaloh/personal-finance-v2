package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.models.TagModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name", "backgroundColorHexCode", "textColorHexCode")
data class TagImportModel(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("backgroundColorHexCode")
    val backgroundColorHexCode: String,
    @JsonProperty("textColorHexCode")
    val textColorHexCode: String
)

fun TagImportModel.toTagModel(): TagModel {
    return TagModel(
        name = this.name,
        glyph = "unknown",
        backgroundColor = backgroundColorHexCode.toColor(),
        textColor = textColorHexCode.toColor()
    )
}