package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.datatypes.WalletType
import com.liamlime.limefinance.api.interfaces.NameableEntity
import java.awt.Color

data class WalletModel(
    override val name: String,
    val glyph: String,
    val textColor: Color,
    val backgroundColor: Color,
    val type: WalletType
): NameableEntity
