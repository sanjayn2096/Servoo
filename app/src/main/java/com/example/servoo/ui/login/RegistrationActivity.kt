package com.example.servoo.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.servoo.MainActivity
import com.example.servoo.R
import com.example.servoo.dao.UserDao
import com.example.servoo.data.model.UserInfo
import com.example.servoo.util.Constants.PHONE_NUMBER
import com.google.gson.Gson
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity() {

    private  lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val email = findViewById<EditText>(R.id.emailInput)
        val firstName = findViewById<EditText>(R.id.firstNameInput)
        val lastName = findViewById<EditText>(R.id.lastNameInput)
        val phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        userDao = UserDao()
        registerButton.setOnClickListener {
            if (validateInputs(firstName.text.toString(), email.text.toString())) {
                val newUser = UserInfo(
                    firstName = firstName.text.toString(),
                    lastName = lastName.text.toString(),
                    phoneNumber = phoneNumber!!,
                    restaurants = listOf(),
                    email = email.text.toString()
                )
                userDao.saveUser(newUser,
                    onSuccess = {
                        Log.d(TAG, "User Saved successfully")
                        // Handle success case
                    },
                    onFailure = { e ->
                        Log.d(TAG,"Error saving user: ${e.message}")
                        // Handle failure case
                    }
                )
                val userInfoJson = Gson().toJson(newUser)
                val bundle = Bundle().apply {
                    putString("userInfo", userInfoJson)
                }
                launchNextActivity(MainActivity::class.java, bundle)
            }

        }

    }

    companion object {
        private const val TAG = "RegistrationActivity"
    }

    private fun validateInputs(firstName: String, email: String) : Boolean{
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        val pattern = Pattern.compile(emailRegex)

        if (firstName.isEmpty()) {
            raiseToast("INVALID FIRST NAME", Toast.LENGTH_LONG)
            return false
        }
        if (email.isEmpty() || !pattern.matcher(email).matches()) {
            raiseToast("INVALID EMAIL", Toast.LENGTH_LONG)
            return false
        }
        return true
    }

    private fun launchNextActivity(activity : Class<*>, bundle: Bundle?) {
        val intent = Intent(this, activity)
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
        finish()
    }

    private fun raiseToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }
}
