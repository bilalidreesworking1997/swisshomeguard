package ch.swisshomeguard.model.status

import com.google.gson.annotations.SerializedName

data class ScheduleStatus(
    @SerializedName("is_schedule") val isScheduleEnabled: Boolean
)