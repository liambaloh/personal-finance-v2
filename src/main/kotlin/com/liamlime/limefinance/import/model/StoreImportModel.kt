package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.models.StoreModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name", "backgroundColorHexCode", "textColorHexCode")
data class StoreImportModel(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("backgroundColorHexCode")
    val backgroundColorHexCode: String,
    @JsonProperty("textColorHexCode")
    val textColorHexCode: String
)

fun StoreImportModel.toStoreModel(): StoreModel {
    return StoreModel(
        name = this.name,
        glyph = "unknown",
        backgroundColor = backgroundColorHexCode.toColor(),
        textColor = textColorHexCode.toColor()
    )
}