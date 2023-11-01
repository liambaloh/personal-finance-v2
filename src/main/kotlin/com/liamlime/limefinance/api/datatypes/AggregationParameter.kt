package com.liamlime.limefinance.api.datatypes

sealed interface AggregationParameter

class ItemAggregator(val aggregationParameter: ItemAggregationParameter) : AggregationParameter
class ReceiptAggregator(val aggregationParameter: ReceiptAggregationParameter) : AggregationParameter