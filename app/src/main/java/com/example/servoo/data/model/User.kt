package com.example.servoo.data.model

data class UserInfo(
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String,
    val restaurants: List<String>,
    val email: String
){
    constructor() : this("", null, "", emptyList(), "")
}
