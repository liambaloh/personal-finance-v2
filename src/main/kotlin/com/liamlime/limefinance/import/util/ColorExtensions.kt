package com.liamlime.limefinance.import.util

import java.awt.Color

fun String.toColor(): Color {
    val red: Int = Integer.valueOf(this.substring(0, 2), 16)
    val green: Int = Integer.valueOf(this.substring(2, 4), 16)
    val blue: Int = Integer.valueOf(this.substring(4, 6), 16)

    return Color(red, green, blue)
}