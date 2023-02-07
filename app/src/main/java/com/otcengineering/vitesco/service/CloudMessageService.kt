package com.otcengineering.vitesco.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.otcengineering.otcble.MySharedPreferences
import com.otcengineering.otcble.remote.MessageService
import com.otcengineering.otcble.remote.Notification
import com.otcengineering.otcble.remote.NotificationBody
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import kotlin.random.Random

class CloudMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.sharedPreferences.putString(Constants.Preferences.FIREBASE_TOKEN, token)
    }

    companion object {
        private const val CHANNELID = "VITESCO"

        fun setupNotificationChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNELID, CHANNELID,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val notificationManager: NotificationManager =
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val msg = message.data["message"] ?: return

        val jsonObject = Gson().fromJson(msg, Notification::class.java)
        val data = Gson().fromJson(jsonObject.data, NotificationBody::class.java)

        val title = MessageService.parseTitle(data.message)
        val body = MessageService.parseMessage(data.message, data.formattedTimestamp())

        showNotification(this, title, body, data.id)
    }

    private fun showNotification(ctx: Context, title: String, content: String, id: Long) {
        val builder = NotificationCompat.Builder(ctx, CHANNELID)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(ctx)) {
            notify(id.toInt(), builder.build())
        }
    }
}