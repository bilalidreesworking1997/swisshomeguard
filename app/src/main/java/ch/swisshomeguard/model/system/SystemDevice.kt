package ch.swisshomeguard.model.system


import com.google.gson.annotations.SerializedName

data class SystemDevice(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("urls") val urls: UrlsX,
    @SerializedName("is_recording") val isRecording: Boolean?
)