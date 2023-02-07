package com.otcengineering.vitesco

import android.app.Application
import android.net.Network
import com.otcengineering.otcble.MySharedPreferences
import com.otcengineering.otcble.NetworkSDK
import com.otcengineering.vitesco.service.CloudMessageService
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants.BASE_URL

class VitescoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        NetworkSDK.BASE_URL = BASE_URL
        Common.network = NetworkSDK(this, BASE_URL)
        Common.sharedPreferences = MySharedPreferences.create(this)

        CloudMessageService.setupNotificationChannel(this)

        Common.loadColors(this)
    }
}