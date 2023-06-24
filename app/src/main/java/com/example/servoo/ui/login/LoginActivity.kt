package com.example.servoo.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.servoo.MainActivity
import com.example.servoo.R
import com.example.servoo.databinding.ActivityLoginBinding
import com.example.servoo.util.Constants.PHONE_NUMBER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Bind Activity to the UI.
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init the FireBase Auth.
        auth = Firebase.auth

        val phoneNumber = binding.phoneNumber
        val getOtpButton = binding.getOtp

        //Connect Activity to the ViewModel
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
                        binding.phoneNumber.focusable = View.FOCUSABLE
                }
                false
            }
        }
        getOtpButton.setOnClickListener {
            loginViewModel.onGetOtpButtonClick(phoneNumber.text.toString())
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "Login Activity onRestart()")
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "Current User Auth = " + FirebaseAuth.getInstance().currentUser)
        if (currentUser != null) {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra("USER_NAME", currentUser.displayName)
            startActivity(mainIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Login Activity onStart()")
    }

    private fun launchNextActivity(phoneNumber:String) {
        Log.println(Log.DEBUG,"LOGIN ACTIVITY","Starting OTP ACTIVITY")
        val otpIntent = Intent(this, OTPActivity::class.java)
        otpIntent.putExtra(PHONE_NUMBER, phoneNumber)
        startActivity(otpIntent)
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
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