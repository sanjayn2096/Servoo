package com.example.servoo.dao

import android.util.Log
import com.example.servoo.data.model.UserInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


class UserDao {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    fun getUserByPhoneNumber(
        phoneNumber: String,
        onSuccess: (UserInfo?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "fetching user by phone number")
        val collection: CollectionReference = db.collection("SERVOO_USERS")
        collection.document(phoneNumber)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    Log.d(TAG, "fetching user by phone number")
                    onSuccess(userInfo)
                } else {
                    Log.d(TAG, "User Not found.")
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun saveUser(userInfo: UserInfo, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (user != null) {
            Log.d(TAG, "User is  authenticated ")
            val collection: CollectionReference = db.collection("SERVOO_USERS")
            collection.document(userInfo.phoneNumber)
                .set(userInfo)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } else {
            // User is not authenticated
            Log.d(TAG, "User is not authenticated")
        }
    }

    companion object {
        private const val TAG = "UserDao"
    }
}
