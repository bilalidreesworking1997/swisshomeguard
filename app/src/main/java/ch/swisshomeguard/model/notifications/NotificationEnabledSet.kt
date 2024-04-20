package ch.swisshomeguard.model.notifications

import com.google.gson.annotations.SerializedName

data class NotificationEnabledSet(
    @SerializedName("user_notification_type_id") val notificationTypeId: Int
)