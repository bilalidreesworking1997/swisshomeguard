package ch.swisshomeguard.model.status

import com.google.gson.annotations.SerializedName

data class AlarmSignalStatus(
    @SerializedName("is_siren") val isSirenEnabled: Boolean,
)