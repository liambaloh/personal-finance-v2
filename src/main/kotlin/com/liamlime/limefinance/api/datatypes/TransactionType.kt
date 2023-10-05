package com.liamlime.limefinance.api.datatypes

import com.liamlime.limefinance.api.interfaces.NameableEntity

enum class TransactionType: NameableEntity {
    UNKNOWN,
    INCOME,
    EXPENSE,
    TRANSFER,
    REIMBURSEMENT;
}