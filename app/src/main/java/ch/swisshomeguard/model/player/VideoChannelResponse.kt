package ch.swisshomeguard.model.player


import com.google.gson.annotations.SerializedName

data class VideoChannelResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("operation") val operation: String,
    @SerializedName("data") val videoChannel: VideoChannel
)