package com.wisnu.kurniawan.composetodolist.foundation.extension

import androidx.appcompat.app.AppCompatDelegate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val zhDateFormatter = DateTimeFormatter.ofPattern("yyyy\u5e74M\u6708d\u65e5", Locale.SIMPLIFIED_CHINESE)
private val zhDateCompactFormatter = DateTimeFormatter.ofPattern("yyyy/M/d", Locale.SIMPLIFIED_CHINESE)
private val zhDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy\u5e74M\u6708d\u65e5 HH:mm", Locale.SIMPLIFIED_CHINESE)
private val zhTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.SIMPLIFIED_CHINESE)

private fun appLocale(): Locale {
    return AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
}

fun LocalDate.toDisplayableDate(locale: Locale = appLocale()): String {
    return if (locale.language == Locale.SIMPLIFIED_CHINESE.language) {
        format(zhDateFormatter)
    } else {
        format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
}

fun LocalDate.toDisplayableDateCompact(locale: Locale = appLocale()): String {
    return if (locale.language == Locale.SIMPLIFIED_CHINESE.language) {
        format(zhDateCompactFormatter)
    } else {
        format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))
    }
}

fun LocalDateTime.toDisplayableDateTime(locale: Locale = appLocale()): String {
    return if (locale.language == Locale.SIMPLIFIED_CHINESE.language) {
        format(zhDateTimeFormatter)
    } else {
        format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale))
    }
}

fun LocalTime.toDisplayableTime(locale: Locale = appLocale()): String {
    return if (locale.language == Locale.SIMPLIFIED_CHINESE.language) {
        format(zhTimeFormatter)
    } else {
        format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    }
}
