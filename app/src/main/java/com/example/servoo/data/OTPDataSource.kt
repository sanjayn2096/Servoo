package com.example.servoo.data

import com.example.servoo.data.model.LoggedInUser
import java.io.IOException
import java.util.*

class OTPDataSource
{
    fun login(phoneNumber: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }

}