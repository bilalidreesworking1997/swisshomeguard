package ch.swisshomeguard.model.status


import com.google.gson.annotations.SerializedName

data class SystemStatus(
    @SerializedName("id") val id: Int,
    @SerializedName("nr") val nr: String,
    @SerializedName("is_recording") val isRecording: Boolean,
    @SerializedName("is_alarm_central") val isAlarmCentral: Boolean,
    @SerializedName("is_schedule") val isSchedule: Boolean,
    @SerializedName("is_siren") val isSiren: Boolean, // If server sends null, it will be regarded as false
    @SerializedName("is_maintenance") val isMaintenance: Boolean,
    @SerializedName("urls") val urls: Urls,
    @SerializedName("editable_options") val editableOptions: EditableOptions,
    @SerializedName("systemNotes") val systemNotes: List<SystemNotes>
)