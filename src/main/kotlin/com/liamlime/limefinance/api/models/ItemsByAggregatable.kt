package com.liamlime.limefinance.api.models

import com.liamlime.limefinance.api.interfaces.NameableEntity

sealed class ItemSelectionOrSelections {
    abstract fun allItems(): List<ItemModel>
}

class Item(
    val content: Map<NameableEntity, ItemSelectionOrSelections>
) : ItemSelectionOrSelections() {
    override fun allItems(): List<ItemModel> {
        return content.values.flatMap { it.allItems() }
    }
}