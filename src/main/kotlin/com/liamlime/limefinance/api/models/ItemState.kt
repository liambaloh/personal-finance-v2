package com.liamlime.limefinance.api.models

import java.time.LocalDateTime

data class ItemState(
    val resolution: ResolutionModel,
    val resolutionDate: LocalDateTime
)
