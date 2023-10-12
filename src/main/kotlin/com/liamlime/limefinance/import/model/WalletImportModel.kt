package com.liamlime.limefinance.import.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.liamlime.limefinance.api.datatypes.toWalletType
import com.liamlime.limefinance.api.models.WalletModel
import com.liamlime.limefinance.import.util.toColor

@JsonPropertyOrder("name", "walletType")
data class WalletImportModel(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("walletType")
    val walletType: String,
)

fun WalletImportModel.toWalletModel(): WalletModel {
    return WalletModel(
        name = this.name,
        glyph = "unknown",
        backgroundColor = "FFFFFF".toColor(),
        textColor = "000000".toColor(),
        type = walletType.toWalletType()
    )
}