package com.liamlime.limefinance.api.model

import java.awt.Color

data class StoreModel(
    override val name: String,
    val glyph: String,
    val textColor: Color,
    val backgroundColor: Color
): NameableEntity
