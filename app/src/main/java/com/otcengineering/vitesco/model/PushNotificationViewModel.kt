package com.otcengineering.vitesco.model

import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import com.otc.alice.api.model.ProfileAndSettings
import com.otc.alice.api.model.Shared
import com.otcengineering.otcble.utils.DateUtils
import com.otcengineering.vitesco.data.PushNotification
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.executeAPI
import com.otcengineering.vitesco.utils.runOnMainThread
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class PushNotificationViewModel {
    var routes = ObservableArrayList<PushNotification>()

    var editRoute = ObservableBoolean(false)
    val routeOptions = ObservableBoolean(false)

    fun deleteSelectedRoutes(ctx: Context) {
        val toDelete = routes.filter { it.selected }
        val count = AtomicInteger(toDelete.size)
        for (route in toDelete) {
            deleteNotification(route.id).subscribe {
                val cnt = count.decrementAndGet()
                if (cnt == 0) {
                    runOnMainThread {
                        routes.clear()
                        getNotificationList(ctx)
                    }
                }
            }
        }
        showRouteOptions(false)
        showEditRoute(false)
    }

    fun showEditRoute(edit: Boolean) {
        editRoute.set(edit)
        for (model in routes) {
            model.setExpansion(edit)
        }
    }

    fun showRouteOptions(show: Boolean) {
        routeOptions.set(show)
    }

    fun splitByTokenIgnoringLast(s: String, token: String): String {
        if (!s.contains(token)) {
            return s
        }
        val split: MutableList<String> = s.split(token) as MutableList<String>
        split.removeLast()
        return split.joinToString("").removeSuffix(" ")
    }

    fun splitCamelCase(s: String): String {
        return s.replace(
            String.format(
                "%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ).toRegex(),
            " "
        )
    }

    fun getNotificationList(ctx: Context) {
        executeAPI(null) {
            val resp = Common.network.profileApi.getNotificationList(1).execute().body() ?: return@executeAPI
            if (resp.status == Shared.OTCStatus.SUCCESS) {
                val rsp = resp.data.unpack(ProfileAndSettings.UserNotifications::class.java)
                for (dtc in rsp.notificationsList) {
                    var before = splitByTokenIgnoringLast(dtc.description, "_")
                    val title = dtc.title

                    if (before.contains("EngineOn") || before.contains("PowerOn")) {
                        before = before.replace("EngineOn", "Engine").replace("PowerOn", "Power")
                    }

                    val name = if (title == "ROUTE") {
                        "Trip"
                    } else {
                        "Device Status"
                    }

                    val route = PushNotification(ctx, dtc.id, name, splitCamelCase(before), dtc.timestamp, false)
                    runOnMainThread {
                        routes.add(route)
                    }

                    /*if (dtc.description.substringBefore("_") == "OilEngine"){
                        val route = PushNotification(dtc.id, "Oil Engine", dtc.description, dtc.timestamp, false)
                        runOnMainThread {
                            routes.add(route)
                        }
                    } else if (dtc.description.substringBefore("_") == "AirFilter") {
                        val route = PushNotification(dtc.id, "Air Filter", dtc.description, dtc.timestamp, false)
                        runOnMainThread {
                            routes.add(route)
                        }
                    } else {
                        val route = PushNotification(dtc.id, "Tracklog", dtc.description, dtc.timestamp, false)
                        runOnMainThread {
                            routes.add(route)
                        }
                    }*/
                }
            }
        }
    }

    fun getNotification(): Observable<String> {
        return Observable.create {
            executeAPI(it) {
                val resp = Common.network.profileApi.getNotificationList(1).execute().body() ?: return@executeAPI
                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = resp.data.unpack(ProfileAndSettings.UserNotifications::class.java)
                    val notif = rsp.notificationsList.first()

                    val before = notif.description.substringBefore("_")
                    val status = if (notif.description.split("_").size > 2) {
                        notif.description.split("_")[1]
                    } else {
                        ""
                    }
                    val date = rsp.notificationsList.first().timestamp

                    val name = when (val title = rsp.notificationsList.first().title) {
                        "DIAGNOSIS_LOG_NOTIFICATION" -> before
                        "VEHICLE_NOTIFICATION" -> "${before}${if (status.isNotEmpty()) " $status" else "" }"
                        "ROUTE" -> "Trip ${rsp.notificationsList.first().description}"
                        else -> {title}
                    }

                    it.onNext("$name at ${DateUtils.serverDateParser(date, "dd/MM/yyyy - HH:mm:ss")}")
                }
            }
        }
    }

    private fun deleteNotification(idNotification : Long): Observable<Unit> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.profileApi.deleteNotification(idNotification).execute().body()
                observable.onNext(Unit)
            }
        }
    }
}