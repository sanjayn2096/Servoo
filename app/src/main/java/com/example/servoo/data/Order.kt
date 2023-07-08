package com.example.servoo.data

data class Order(
    val id: String,
    val item : List<FoodMenuItem>,
    val orderStatus: OrderStatus,
    val orderDescription: String?
    // Other order properties
) {
    // Optional additional methods or functionality
}

