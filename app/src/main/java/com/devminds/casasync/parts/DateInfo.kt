package com.devminds.casasync.parts

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
import java.time.ZoneId

data class DateInfo(
    val tomorrow: String,
    val today: String,
    val yesterday: String,
    val hour: Int,
    val minute: Int,
    val hourMinute: String,
    val fullDate: String,

    val dayPlus: String,
    val dayMinus: String
)

// pode retornar vários tipos de data
fun date(days: Long): DateInfo { // days representa os dias para somar ou subtrair
    val dateNow = LocalDateTime.now()
    val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val tomorrow = dateNow.plusDays(1).format(formatterDate)
    val today = dateNow.format(formatterDate)
    val yesterday = dateNow.minusDays(1).format(formatterDate)
    val hour = dateNow.format(DateTimeFormatter.ofPattern("HH")).toInt()
    val minute = dateNow.format(DateTimeFormatter.ofPattern("mm")).toInt()
    val hourMinute = dateNow.format(DateTimeFormatter.ofPattern("HH:mm"))
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val fullDate = dateNow.format(formatter)

    val dayPlus = dateNow.plusDays(days).format(formatterDate) // soma dias
    val dayMinus = dateNow.minusDays(days).format(formatterDate) // diminui dias

    return DateInfo(tomorrow, today, yesterday, hour, minute, hourMinute, fullDate, dayPlus, dayMinus)
}

fun hourPicker(message: String): MaterialTimePicker {
    val date = date(0)
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

fun formatter(date: String?, hour: String?): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    val safeDate = if (date.isNullOrBlank()) "01/01/1970" else date
    val safeHour = if (hour.isNullOrBlank()) "00:00" else hour

    return LocalDateTime.parse("$safeDate $safeHour", formatter)
}

fun prevDateMillis(date: LocalDateTime): Long {
    val prevDateHourMillis = date
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    return prevDateHourMillis
}

fun minusHour(previsionDate: String?, previsionHour: String?, hours: Long): Long {
    val previsionDateTime = formatter(previsionDate, previsionHour)

    val notifyTime = previsionDateTime.minusHours(hours)
    Log.d("Menos 1 hora", notifyTime.toString())
    val notifyMillis = notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return notifyMillis
}

fun minusDay(previsionDate: String?, previsionHour: String?, days: Long): Long {
    val previsionDateTime = formatter(previsionDate, previsionHour)

    val notifyTime = previsionDateTime.minusDays(days)
    Log.d("Menos 1 dia", notifyTime.toString())
    val notifyMillis = notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return notifyMillis
}
