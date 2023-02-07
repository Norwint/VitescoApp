package com.otcengineering.vitesco.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.otcengineering.otcble.MySharedPreferences
import com.otcengineering.otcble.NetworkSDK
import com.otcengineering.vitesco.R

object Common {
    fun loadColors(ctx: Context) {
        colorRed = ContextCompat.getColor(ctx, R.color.colorRed)
        colorGreen = ContextCompat.getColor(ctx, R.color.colorGreen)
        colorBlue = ContextCompat.getColor(ctx, R.color.colorBlue)
        colorOrange = ContextCompat.getColor(ctx, R.color.colorOrange)
        colorYellow = ContextCompat.getColor(ctx, R.color.colorYellow)
    }

    lateinit var network: NetworkSDK
    lateinit var sharedPreferences: MySharedPreferences
    var serialNumber = ""
    var macAddress = ""

    var colorRed = 0
    var colorGreen = 0
    var colorBlue = 0
    var colorOrange = 0
    var colorYellow = 0
}