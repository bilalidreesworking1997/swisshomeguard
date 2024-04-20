package ch.swisshomeguard.model.status


import com.google.gson.annotations.SerializedName

data class SystemStatusResponse(
    @SerializedName("data")
    val systemStatus: SystemStatus,
    @SerializedName("operation")
    val operation: String,
    @SerializedName("success")
    val success: Boolean
)