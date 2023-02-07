package com.otcengineering.vitesco.utils

import java.time.*
import java.time.format.DateTimeFormatter

object TimeUtils {
    fun parseDurationToHoursMinutes(duration: Duration) : String {
        val minutes = duration.toMinutes()
        val hours = minutes / 60
        var result = ""

        if (hours > 0) {
            result += String.format("%02dh ", hours)
        }
        result += String.format("%02dmin", minutes % 60)

        return result
    }

    fun serverTimeParse(serverTime: String, fmt: String): String {
        val fmtrIn = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fmtrOut = DateTimeFormatter.ofPattern(fmt)
        val date = LocalDateTime.parse(serverTime, fmtrIn)
        return date.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).format(fmtrOut)
    }

    fun getTimeForServer(): String {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val fmtr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return now.format(fmtr)
    }

    fun parseSecondsToTime(seconds: Int): String {
        val minutes = (seconds / 60) % 60
        val hours = seconds / 3600
        var result = ""

        if (hours > 0) {
            result += String.format("%dfh ", hours)
        }

        if (minutes > 0) {
            result += String.format("%dm ", minutes)
        }

        result += String.format("%ds", seconds % 60)
        return result
    }

    fun parseMinutesToHoursMinutes(duration: Int): String {
        val hours = duration / 60
        var result = ""

        if (hours > 0) {
            result += String.format("%dh ", hours)
        }
        result += String.format("%dm", duration % 60)

        return result
    }
}