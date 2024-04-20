package ch.swisshomeguard.model.notifications


import com.google.gson.annotations.SerializedName

data class UserNotificationType(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?
)