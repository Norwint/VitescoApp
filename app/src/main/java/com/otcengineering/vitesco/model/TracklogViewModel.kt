package com.otcengineering.vitesco.model

import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import com.otc.alice.api.model.MyTrip.BodyVariable
import com.otc.alice.api.model.MyTrip.BodyVariableResponse
import com.otc.alice.api.model.MyTrip.Routes
import com.otc.alice.api.model.MyTrip.RoutesResponse
import com.otc.alice.api.model.Shared
import com.otcengineering.otcble.utils.DateUtils
import com.otcengineering.vitesco.data.Tracklog
import com.otcengineering.vitesco.utils.*
import io.reactivex.rxjava3.core.Observable
import java.io.IOException
import java.text.DateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TracklogViewModel() {
    var routes = ObservableArrayList<Tracklog>()
    val routeLoading = ObservableBoolean()
    val routesAvailable = ObservableBoolean()

    //private val _editRoute = MutableLiveData(false)
    val editRoute = ObservableBoolean(false)
    //private val _routeOptions = MutableLiveData(false)
    val routeOptions = ObservableBoolean(false)

    fun loadRoutes(page: Int, ctx: Context) {
        routeLoading.set(true)
        getRoutes(page,ctx)
    }

    private fun List<Tracklog>.doDeleteRoutes(){

    }

    fun deleteTracklog(idRoute : Long): Observable<Unit> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.tripApi.deleteRoute(idRoute).execute().body()
                observable.onNext(Unit)
            }
        }
    }

    fun deleteSelectedRoutes() {
        val toDelete = routes.filter { it.selected }
        val count = AtomicInteger(toDelete.size)
        for (route in toDelete) {
            deleteTracklog(route.tracklogID).subscribe {
                val cnt = count.decrementAndGet()
                if (cnt == 0) {
                    runOnMainThread {
                        routes.clear()
                    }
                }
            }
        }
        routes.removeAll(toDelete.toSet())
        showRouteOptions(false)
        showEditRoute(false)
        routesAvailable.set(routes.size > 0)
    }

    fun deleteRoute(route: Tracklog) {
        listOf(route).doDeleteRoutes()
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

    private fun getRoutes(page: Int, ctx: Context) {
        executeAPI(null) {
            val resp = Common.network.tripApi.getAllRoutes(page, Common.sharedPreferences.getString(Constants.Preferences.VEHICLE_ID).toLong()).execute().body()

            runOnMainThread {
                if (resp == null || resp.status != Shared.OTCStatus.SUCCESS) {
                    routeLoading.set(false)
                    return@runOnMainThread
                } else {
                    val routes = resp.data.unpack(RoutesResponse::class.java)

                    if (routes.totalPages < routes.currentPage) {
                        return@runOnMainThread
                    }
                    for (route in routes.routesList) {
                        val tracklog = Tracklog(
                            ctx,
                            String.format(Locale.US, "%01.02fh", route.operationalWorkingHoursStart),
                            String.format(Locale.US, "%01.02fh", route.operationalWorkingHoursEnd),
                            String.format("%01.02fh", route.duration / 60.0F),
                            route.id,
                            route.ecoDrivingScore,
                            route.drivingScore1.score,
                            route.drivingScore1.speedScore,
                            route.drivingScore1.timeScore,
                            route.drivingScore1.throttleScore,
                            route.drivingScore1.consumeScore,
                            route.drivingScore1.rpmScore,
                            route.drivingScore1Avg.scoreAvg,
                            route.ecoDrivingScoreAvg,
                            route.drivingScore1Avg.speedScoreAvg,
                            route.drivingScore1Avg.timeScoreAvg,
                            route.drivingScore1Avg.throttleScoreAvg,
                            route.drivingScore1Avg.consumeScoreAvg,
                            route.drivingScore1Avg.rpmScoreAvg,
                            String.format(Locale.US, "%01.03f l", route.fuelConsumption),
                            route.distance,
                            route.uploadDate,
                            route.duration
                        )
                        this.routes.add(tracklog)
                    }
                }
                routeLoading.set(false)
                routesAvailable.set(this.routes.size > 0)
            }
        }
    }

    fun getDataOfVariable(variable: String, routeID: Long): Observable<BodyVariableResponse> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.tripApi.getTracklogVariable(variable, routeID).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null"))
                    return@executeAPI
                }

                if (resp.status != Shared.OTCStatus.SUCCESS) {
                    observable.onError(OtcException(resp.status))
                    return@executeAPI
                }

                val bodyVar = resp.data.unpack(BodyVariableResponse::class.java)
                observable.onNext(bodyVar)
            }
        }
    }

//    fun createDummy() {
//        val date = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss")
//
//        for (i in 0L until 10L) {
//            val dateStart = date.minusDays(i + 1)
//            val dateEnd = dateStart.plusMinutes(10 * (i + 1))
//            val duration = Duration.between(dateStart, dateEnd)
//            val durationString = TimeUtils.parseDurationToHoursMinutes(duration)
//            val route = Tracklog(dateStart.format(formatter), dateEnd.format(formatter), durationString)
//            routes.add(route)
//        }
//
//        routeLoading.set(false)
//        routesAvailable.set(routes.size > 0)
//    }
}