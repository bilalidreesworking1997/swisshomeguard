package ch.swisshomeguard.model.events


import com.google.gson.annotations.SerializedName

data class EventRecording(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("event_created_at")
    val eventCreatedAt: String,
    @SerializedName("event_id")
    val eventId: Int,
    @SerializedName("file_path")
    val filePath: String,
    @SerializedName("file_size")
    val fileSize: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("ip")
    val ip: Any,
    @SerializedName("preview_path")
    val previewPath: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("urls")
    val urls: Urls
)