package com.otcengineering.vitesco.data

import android.graphics.drawable.Drawable
import android.view.View
import java.util.*

class DashboardFilters(
    val name: String,
    private val lastChange: String,
    private val workingHours: Float,
    private val error: Boolean,
    val onButtonClick: () -> Unit) {

    companion object {
        var detectedString = ""
        var replacedString = ""
        var errorIcon: Drawable? = null
        var okIcon: Drawable? = null
        var errorColor: Int = 0
        var okColor: Int = 0
    }

    fun lastChangeFormatted() = if (lastChange.isEmpty()) "" else String.format(if (error) detectedString else replacedString, lastChange)
    fun workingHoursFormatted(): String = if (workingHours == 0.0F ) "" else "Working Hours: ${String.format(
        Locale.US, "%01.02f", workingHours)}h"

    fun visibilityButton(): Int = if (error) View.VISIBLE else View.GONE

    fun statusColor(): Int = if (error) errorColor else okColor
    fun statusImage(): Drawable? = if (error) errorIcon else okIcon
}