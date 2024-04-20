package ch.swisshomeguard.model.image


import com.google.gson.annotations.SerializedName

data class ImagePath(
    @SerializedName("image_available_until")
    val imageAvailableUntil: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("refresh_interval")
    val refreshInterval: Int
)