package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class NotificationEnabledStatus(
    @SerializedName("user_notification_type_id") val userNotificationTypeId: Int
)