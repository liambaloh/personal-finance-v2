package com.liamlime.limefinance.api.model

import java.awt.Color

data class ResolutionModel(
    override val name: String,
    val glyph: String,
    val textColor: Color,
    val backgroundColor: Color
): NameableEntity
