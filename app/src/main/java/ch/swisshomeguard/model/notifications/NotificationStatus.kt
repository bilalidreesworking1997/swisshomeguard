package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class NotificationStatus(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("device_model")
    val deviceModel: String,
    @SerializedName("device_platform")
    val devicePlatform: String,
    @SerializedName("firebase_id")
    val firebaseId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int
)