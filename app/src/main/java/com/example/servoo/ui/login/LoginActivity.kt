package com.example.servoo.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.servoo.databinding.ActivityLoginBinding

import com.example.servoo.R
import com.example.servoo.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var otpBinding: ActivityOtpBinding
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        val phoneNumber = binding.phoneNumber
        val getOtpButton = binding.getOtp

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            if (loginState.phoneNumberError != null) {
                phoneNumber.error = getString(loginState.phoneNumberError)
            }
        })

        loginViewModel.clickEvent.observe(this) { clicked ->
            if (clicked) {
                // The click event has occurred, perform the desired action
                launchNextActivity(phoneNumber.text.toString())
            } else {
                Toast.makeText(this, R.string.invalid_phonenumber, Toast.LENGTH_SHORT).show()
            }
        }


        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        phoneNumber.afterTextChanged {
            loginViewModel.loginDataChanged(
                phoneNumber.text.toString()
            )
        }

        phoneNumber.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    phoneNumber.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
//                        loginViewModel.login(
//                            phoneNumber.text.toString()
//                        )
                        loginViewModel.onGetOtpButtonClick(phoneNumber.text.toString())
                }
                false
            }
        }

        getOtpButton.setOnClickListener {
            loginViewModel.onGetOtpButtonClick(phoneNumber.text.toString())
        }
    }


    private fun launchNextActivity(phoneNumber:String) {
        Log.println(Log.DEBUG,"LOGIN ACTIVITY","Starting OTP ACTIVITY")
        val otpIntent = Intent(this, OTPActivity::class.java)
        otpIntent.putExtra("PHONE_NUMBER", phoneNumber)
        startActivity(otpIntent)
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}