package ch.swisshomeguard

import android.util.Log
import ch.swisshomeguard.utils.createNavToEventPlayerNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val data = remoteMessage.data

        Log.d(FCM_TAG, "Notification title: $title")
        Log.d(FCM_TAG, "Notification body: $body")
        Log.d(FCM_TAG, "Data payload: $data")

        if (title != null && body != null) {
            createNavToEventPlayerNotification(applicationContext, title, body, data)
        }
    }
}