package com.example.servoo.service

import MainActivity
import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class OrderStatusService : Service() {
    private var firestore: FirebaseFirestore? = null
    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Set up real-time listener for order updates
        listenForOrderUpdates()

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Status",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Order Status")
            .setContentText("Listening for order updates")
            .setSmallIcon(R.drawable.ic_notification_overlay)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun listenForOrderUpdates() {
        firestore!!.collection("orders")
            .addSnapshotListener { queryDocumentSnapshots: QuerySnapshot?, e: Exception? ->
                if (e != null) {
                    Log.e(
                        TAG,
                        "Error listening for order updates: " + e.message
                    )
                    return@addSnapshotListener
                }
                for (documentChange in queryDocumentSnapshots!!.documentChanges) {
                    // Handle the order updates here
                }
            }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "OrderStatusService"
        private const val CHANNEL_ID = "OrderStatusChannel"
        private const val NOTIFICATION_ID = 1
    }
}
