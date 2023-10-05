package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.interfaces.NameableEntity

data class CurrencyModel(
    override val name: String
): NameableEntity