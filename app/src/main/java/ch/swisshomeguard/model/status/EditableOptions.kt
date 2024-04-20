package ch.swisshomeguard.model.status


import com.google.gson.annotations.SerializedName

data class EditableOptions(
    @SerializedName("is_schedule") val isSchedule: Boolean,
    @SerializedName("is_alarm_central") val isAlarmCentral: Boolean,
    @SerializedName("is_recording") val isRecording: Boolean,
    @SerializedName("is_siren") val isSiren: Boolean,
    @SerializedName("is_maintenance") val isMaintenance: Boolean,
    @SerializedName("is_locked") val isLocked: Boolean
)