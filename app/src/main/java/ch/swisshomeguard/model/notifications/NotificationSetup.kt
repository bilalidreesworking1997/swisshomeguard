package ch.swisshomeguard.model.notifications

import com.google.gson.annotations.SerializedName

data class NotificationSetup(
    @SerializedName("firebase_id") val firebaseId: String,
    @SerializedName("device_model") val deviceModel: String,
    @SerializedName("device_platform") val devicePlatform: String = "android"
)