package ch.swisshomeguard.model.status

import com.google.gson.annotations.SerializedName

data class AlarmCentralStatus(
    @SerializedName("is_alarm_central") val isAlarmCentralEnabled: Boolean,
)