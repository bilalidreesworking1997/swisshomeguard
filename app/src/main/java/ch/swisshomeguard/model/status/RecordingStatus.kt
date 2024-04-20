package ch.swisshomeguard.model.status

import com.google.gson.annotations.SerializedName

data class RecordingStatus(
    @SerializedName("is_recording") val isRecordingEnabled: Boolean,
)