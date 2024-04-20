package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class NotificationAdditionalData(
    @SerializedName("userNotificationType") val userNotificationType: List<UserNotificationType>
)