package com.example.servoo.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OTPViewModel : ViewModel() {
    // Define MutableLiveData for OTP input and a LiveData for navigation event
    val otpInput = MutableLiveData<String>()
    private val _navigateToNext = MutableLiveData<Unit>()
    val navigateToNext: LiveData<Unit> = _navigateToNext
    val clickEvent = MutableLiveData<Boolean>()

    fun onVerifyButtonClick() {
        // Perform OTP verification logic here
        // If the OTP is valid, trigger the navigation event
        _navigateToNext.value = Unit
    }

    fun onGetOtpButtonClick(otp : String) {
        if(isOTPValid(otp)) {
            clickEvent.value = true
        } else {
            Log.println(Log.DEBUG,"OTPViewModel","OTP Not Valid")
            clickEvent.value = false
        }
    }

    private fun isOTPValid (otp : String): Boolean {
        return otp.length == 6
    }

}
