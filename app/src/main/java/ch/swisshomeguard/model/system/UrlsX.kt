package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class UrlsX(
    @SerializedName("preview") val preview: String?,
    @SerializedName("preview_refresh_interval") val previewRefreshInterval: Int,
    @SerializedName("stream_1") val stream1: String?,
    @SerializedName("stream_2") val stream2: String?
)