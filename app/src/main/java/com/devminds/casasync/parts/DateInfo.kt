package com.devminds.casasync.parts

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class DateInfo(
    val hour: Int,
    val minute: Int,
    val fullDate: String
)

fun date(): DateInfo {
    val dateNow = LocalDateTime.now()
    val hour = dateNow.format(DateTimeFormatter.ofPattern("HH")).toInt()
    val minute = dateNow.format(DateTimeFormatter.ofPattern("mm")).toInt()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val fullDate = dateNow.format(formatter)

    return DateInfo(hour, minute, fullDate)
}

fun hourPicker(message: String): MaterialTimePicker {
    val date = date()
    val hourPicker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_24H) // tipo de relógio
        .setHour(date.hour) // hora atual
        .setMinute(date.minute) // minuto atual
        .setTitleText(message)
        .build()

    return hourPicker
}

fun datePicker(message: String): MaterialDatePicker<Long> {
    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(message)
        .build()

    return datePicker
}
