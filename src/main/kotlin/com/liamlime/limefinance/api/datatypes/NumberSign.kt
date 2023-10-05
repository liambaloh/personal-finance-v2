package com.liamlime.limefinance.api.datatypes

import com.liamlime.limefinance.api.interfaces.NameableEntity
import java.math.BigDecimal

enum class NumberSign(): NameableEntity{
    POSITIVE, NEGATIVE, ZERO
}

fun BigDecimal.sign(): NumberSign = when {
    this > BigDecimal.ZERO -> NumberSign.POSITIVE
    this < BigDecimal.ZERO -> NumberSign.NEGATIVE
    else -> NumberSign.ZERO
}