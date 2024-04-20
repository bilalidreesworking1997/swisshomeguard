package ch.swisshomeguard.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*

// TODO Improve all methods in the class

// https://stackoverflow.com/questions/45452791/convert-yyyy-mm-ddthhmmss-mmmz-to-normal-hhmm-a-format
@SuppressLint("SimpleDateFormat")
fun convertTimeDate(context: Context, s: String): String {
    // Server date time format
    val inputTimeDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // treat "Z" as literal
    inputTimeDateFormat.timeZone = TimeZone.getTimeZone("UTC") // use UTC as timezone
    val inputDate = inputTimeDateFormat.parse(s)

    val outputDateFormat = DateFormat.getDateFormat(context)
    val outputDate = outputDateFormat.format(inputDate)

    val outputTimeFormat = DateFormat.getTimeFormat(context)
    val outputTime = outputTimeFormat.format(inputDate)

    return "$outputDate\n$outputTime"
}

@SuppressLint("SimpleDateFormat")
fun convertDate(context: Context, s: String): String {
    // Server date time format
    val inputTimeDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // treat "Z" as literal
    inputTimeDateFormat.timeZone = TimeZone.getTimeZone("UTC") // use UTC as timezone
    val inputDate = inputTimeDateFormat.parse(s)

    val outputDateFormat = DateFormat.getDateFormat(context)
    val outputDate = outputDateFormat.format(inputDate)

    return outputDate
}

@SuppressLint("SimpleDateFormat")
fun convertTimeDateSingleLine(context: Context, s: String): String {
    // Server date time format
    val inputTimeDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // treat "Z" as literal
    inputTimeDateFormat.timeZone = TimeZone.getTimeZone("UTC") // use UTC as timezone
    val inputDate = inputTimeDateFormat.parse(s)

    val outputDateFormat = DateFormat.getDateFormat(context)
    val outputDate = outputDateFormat.format(inputDate)

    val outputTimeFormat = DateFormat.getTimeFormat(context)
    val outputTime = outputTimeFormat.format(inputDate)

    return "$outputDate $outputTime"
}

fun convertDateInMillisToLocal(time: Long): String {
    return getDateInstance().format(time)
}