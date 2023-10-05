package com.liamlime.limefinance.api.datatype

import com.liamlime.limefinance.api.model.NameableEntity

enum class TransactionType: NameableEntity {
    UNKNOWN,
    INCOME,
    EXPENSE,
    TRANSFER,
    REIMBURSEMENT;
}
