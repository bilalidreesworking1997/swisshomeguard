package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class NotificationEnabledStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("operation") val operation: String,
    @SerializedName("data") val data: NotificationEnabledStatus,
    @SerializedName("additionalData") val additionalData: NotificationAdditionalData
)