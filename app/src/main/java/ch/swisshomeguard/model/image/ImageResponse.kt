package ch.swisshomeguard.model.image


import com.google.gson.annotations.SerializedName

data class ImageResponse(
    @SerializedName("data")
    val imagePath: ImagePath,
    @SerializedName("operation")
    val operation: String,
    @SerializedName("success")
    val success: Boolean
)