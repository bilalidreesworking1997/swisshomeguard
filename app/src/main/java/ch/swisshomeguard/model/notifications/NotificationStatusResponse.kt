package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class NotificationStatusResponse(
    @SerializedName("data")
    val notificationStatus: NotificationStatus,
    @SerializedName("operation")
    val operation: String,
    @SerializedName("success")
    val success: Boolean
)