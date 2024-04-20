package ch.swisshomeguard.model.status


import com.google.gson.annotations.SerializedName

data class SystemNotes(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)