package ch.swisshomeguard.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import ch.swisshomeguard.FCM_TAG
import ch.swisshomeguard.R
import ch.swisshomeguard.ui.player.EventPlayerActivityArgs

const val CHANNEL_ID_EVENT = "channel_id_event"

// Objects expected inside the notification data payload
const val EVENT_ID = "event_id"
const val SYSTEM_ID = "system_id"

fun createNavToEventPlayerNotification(
    context: Context,
    title: String,
    body: String,
    data: Map<String, String>
) {
    val systemId = data[SYSTEM_ID]?.toInt()
    val eventId = data[EVENT_ID]?.toInt()

    if (systemId != null && eventId != null) {
        val bundle = EventPlayerActivityArgs(systemId, eventId).toBundle()
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.eventPlayerActivity)
            .setArguments(bundle)
            .createPendingIntent()
        postNotification(context, title, body, pendingIntent)
    } // TODO else systemId or eventId are missing
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_channel_event)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID_EVENT, name, importance)
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager =
            context.getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(FCM_TAG, "Notification channel created")
    }
}

private fun postNotification(context: Context, title: String, body: String, intent: PendingIntent) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID_EVENT)
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(R.drawable.ic_stat_ic_notification)
        .setColor(ContextCompat.getColor(context, R.color.colorAccent))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)   // required for Android 7.1 and lower
        .setContentIntent(intent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).apply {
        val notificationId = System.currentTimeMillis().toInt()
        notify(notificationId, notification)
    }
}
