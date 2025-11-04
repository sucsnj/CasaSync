package com.devminds.casasync.utils

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

object DatePickers {
    fun hourPicker(message: String): MaterialTimePicker {
        val date = DateUtils.date(0)
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
}
