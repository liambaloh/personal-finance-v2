package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.interfaces.NameableEntity
import java.awt.Color

data class PortfolioModel(
    override val name: String,
    val glyph: String,
    val textColor: Color,
    val backgroundColor: Color
): NameableEntity
