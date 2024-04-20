package ch.swisshomeguard.model.player


import com.google.gson.annotations.SerializedName

data class VideoChannel(
    @SerializedName("stream_url") val streamUrl: String,
    @SerializedName("stream_alive_until") val streamAliveUntil: String,
    @SerializedName("keep_alive_url") val keepAliveUrl: String,
    @SerializedName("keep_alive_interval") val keepAliveInterval: Int
)