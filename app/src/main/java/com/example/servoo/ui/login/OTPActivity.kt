package com.example.servoo.ui.login

import MainActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.servoo.R
import com.example.servoo.dao.UserDao
import com.example.servoo.databinding.ActivityOtpBinding
import com.example.servoo.util.Constants.PHONE_NUMBER
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {
    private lateinit var viewModel: OTPViewModel
    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var  userDao: UserDao
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        val extras = intent.extras
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        val verifyOtpButton = binding.verifyOtp
        val otp = binding.enterOtp

        callbacks = object : OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(this@OTPActivity, R.string.invalid_OTP, Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }
            }


            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        if (extras != null) {
            val phoneNumber = extras.getString(PHONE_NUMBER) // Replace "extra_key" with the actual key you used to put the extra in the Intent
            // Use the extra value as needed
            this.phoneNumber = phoneNumber!!
            binding.phoneNumberInOtp?.setText(phoneNumber)
            startPhoneNumberVerification(phoneNumber, callbacks)
        }

        viewModel = ViewModelProvider(this).get(OTPViewModel::class.java)

        // Observe the navigation event from the ViewModel
        viewModel.navigateToNext.observe(this) {
        }

        viewModel.clickEvent.observe(this) { clicked ->
            if (clicked) {
                Log.d("storedVerificationId = " + storedVerificationId, " otp = " + otp?.text.toString())
                val credential = verifyPhoneNumberWithCode(storedVerificationId, otp?.text.toString())
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, R.string.invalid_phonenumber, Toast.LENGTH_SHORT).show()
            }
        }

        verifyOtpButton?.setOnClickListener {
            viewModel.onGetOtpButtonClick(otp?.text.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun startPhoneNumberVerification(phoneNumber: String, callbacks: OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
    }

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    userDao = UserDao()
                    userDao.getUserByPhoneNumber(phoneNumber,
                        onSuccess = { userInfo ->
                            // Handle the retrieved user information
                            if (userInfo != null) {
                                Log.d(TAG, "User found: ${userInfo.firstName} ${userInfo.lastName}")
                                val userInfoJson = Gson().toJson(userInfo)
                                val bundle = Bundle().apply {
                                    putString("userInfo", userInfoJson)
                                }
                                launchNextActivity(MainActivity::class.java, bundle)
                            } else {
                                Log.d(TAG,"User not found.")
                                val bundle = Bundle().apply {
                                    putString(PHONE_NUMBER, phoneNumber)
                                }
                                launchNextActivity(RegistrationActivity::class.java, bundle)
                            }
                        },
                        onFailure = { e ->
                            // Handle the error
                            println("Failed to retrieve user information: ${e.message}")
                        }
                    )
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d(TAG, "The verification code entered was invalid")
                    }
                    // Update UI
                }
            }
    }
    // [END sign_in_with_phone]

    private fun verifyPhoneNumberWithCode(
        verificationId: String?,
        code: String
    ): PhoneAuthCredential {
        // [START verify_with_code]
        return PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?,
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // (optional) Activity for callback binding
            // If no activity is passed, reCAPTCHA verification can not be used.
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }
    // [END resend_verification]

    private fun launchNextActivity(activity : Class<*>, bundle: Bundle?) {
        val intent = Intent(this, activity)
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "OTPActivity"
    }

}

