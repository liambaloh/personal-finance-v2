package com.liamlime.limefinance.api.datatypes

import com.liamlime.limefinance.api.interfaces.NameableEntity

enum class WalletType: NameableEntity {
    UNKNOWN, CASH, SAVINGS, CAPITAL
}

fun String.toWalletType(): WalletType {
    return when (this.trim().uppercase()) {
        "CASH" -> WalletType.CASH
        "SAVINGS" -> WalletType.SAVINGS
        "CAPITAL" -> WalletType.CAPITAL
        else -> WalletType.UNKNOWN
    }
}