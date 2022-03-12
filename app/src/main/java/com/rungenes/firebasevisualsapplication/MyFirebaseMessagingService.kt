package com.rungenes.firebasevisualsapplication

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rungenes.firebasevisualsapplication.MyFirebaseMessagingService
import java.lang.NullPointerException

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var notificationBody: String? = ""
        var notificationTitle: String? = ""
        var notificationData = ""
        try {
            notificationBody = remoteMessage.notification!!.body
            notificationTitle = remoteMessage.notification!!.title
            notificationData = remoteMessage.data.toString()
        } catch (e: NullPointerException) {
            Log.e(TAG, "onMessageReceived:NullPointerException: " + e.message)
        }
        Log.d(TAG, "onMessageReceived: data: $notificationData")
        Log.d(TAG, "onMessageReceived: Body: $notificationBody")
        Log.d(TAG, "onMessageReceived: Title :$notificationTitle")
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingServ"
    }
}