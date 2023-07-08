package com.example.servoo.data

data class Restaurant(
    val name : String,
    val foodMenu : FoodMenu,
    val ownerName: String,
    val address: String,
    val phoneNumber: String,
    val emailAddress: String,
    val verificationStatus: VerificationStatus
)

enum class VerificationStatus{
    VERIFIED,
    PENDING,
    BLOCKED
}

