package com.example.servoo.data.model

import com.example.servoo.data.Restaurant

data class UserInfo(
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String,
    val restaurants: List<Restaurant>,
    val email: String
){
    constructor() : this("", null, "", emptyList(), "")
}
