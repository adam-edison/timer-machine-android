package xyz.aprildown.timer.app.base.utils

import java.util.Locale

/**
 * Time Calculation and Formation
 */

fun Long.produceHms(): Triple<Int, Int, Int> {
    var time = this
    if (time < 0) {
        time = -time
    }

    var seconds = time / 1000
    var minutes = seconds / 60
    seconds -= minutes * 60
    val hours = minutes / 60
    minutes -= hours * 60
    // if (hours > 999) {
    //     hours = 0
    // }
    return Triple(hours.toInt(), minutes.toInt(), seconds.toInt())
}

private const val TWO_DIGITS = "%02d"
private const val ONE_DIGIT = "%01d"

fun Long.produceTime(): String {
    val (hours, minutes, seconds) = this.produceHms()

    // Hours may be empty
    val mHours = when {
        hours >= 10 -> String.format(Locale.getDefault(), TWO_DIGITS, hours)
        hours > 0 -> String.format(Locale.getDefault(), ONE_DIGIT, hours)
        else -> null
    }

    // Minutes are never empty and when hours are non-empty, must be two digits
    // mMinutes = if (minutes >= 10 || hours > 0) {
    //     String.format(Locale.getDefault(), TWO_DIGITS, minutes)
    // } else {
    //     String.format(Locale.getDefault(), ONE_DIGIT, minutes)
    // }
    // I prefer that minutes also have two digits.
    val mMinutes = String.format(Locale.getDefault(), TWO_DIGITS, minutes)

    // Seconds are always two digits
    val mSeconds = String.format(Locale.getDefault(), TWO_DIGITS, seconds)

    val builder = StringBuilder()
    if (mHours != null) {
        builder.append(mHours).append(':')
    }
    builder.append(mMinutes).append(':')
    builder.append(mSeconds)

    return builder.toString()
}

/**
 * Compact form with no leading zeros or separators, only non-zero units shown,
 * e.g. "3h10m4s", "1h", "1h4m", "54s". A zero duration produces "0s".
 */
fun Long.produceCompactTime(): String {
    val (hours, minutes, seconds) = this.produceHms()

    val builder = StringBuilder()
    if (hours > 0) builder.append(hours).append('h')
    if (minutes > 0) builder.append(minutes).append('m')
    if (seconds > 0 || builder.isEmpty()) builder.append(seconds).append('s')

    return builder.toString()
}
