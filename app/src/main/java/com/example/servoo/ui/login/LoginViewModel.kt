package com.example.servoo.ui.login

import android.content.Context
import android.util.Log
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import android.widget.Toast
import com.example.servoo.data.LoginRepository
import com.example.servoo.data.Result

import com.example.servoo.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val clickEvent = MutableLiveData<Boolean>()

    fun login(phoneNumber: String) {
        val result = loginRepository.login(phoneNumber)
        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun onGetOtpButtonClick(phoneNumber: String) {
        if(isPhoneNumberValid(phoneNumber)) {
            Log.println(Log.DEBUG,"LOGIN VIEW MODEL","phone number Not Valid")
            clickEvent.value = true
        } else {
            clickEvent.value = false
        }
    }

    fun loginDataChanged(phoneNumber: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            _loginForm.value = LoginFormState(phoneNumberError = R.string.invalid_phonenumber)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.length == 10;
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}