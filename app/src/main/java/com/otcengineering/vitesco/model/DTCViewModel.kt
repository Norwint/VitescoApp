package com.otcengineering.vitesco.model

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableArrayList
import com.otc.alice.api.model.DashboardAndStatus
import com.otc.alice.api.model.Shared
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.DTC
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.executeAPI
import com.otcengineering.vitesco.utils.runOnMainThread
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.random.Random

class DTCViewModel(private val ctx: Context) {
    var routes = ObservableArrayList<DTC>()
    var pages = 0
    var currentPages = 0

    init {
        getDTCHistory(Common.sharedPreferences.getLong(Constants.Preferences.VEHICLE_ID, 0L),"all","-1", 1)
        DTC.colorEnabled = ContextCompat.getColor(ctx, R.color.colorRed)
        DTC.colorDisabled = ContextCompat.getColor(ctx, R.color.colorGreen)
        DTC.colorCleared = ContextCompat.getColor(ctx, R.color.colorYellow)
    }

    fun clear() {
        routes.clear()
    }

    fun getDTCHistory(vehID: Long, filter: String, filterCode: String, page: Int): Observable<Int> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.dashboardApi.getDTCHistory(vehID, filter, filterCode,
                    page
                ).execute().body() ?: return@executeAPI

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = resp.data.unpack(DashboardAndStatus.DtcHistoryResponse::class.java)
                    Log.d("fields", rsp.allFields.toString())
                    pages = rsp.totalPages
                    pages = Constants.Preferences.TOTAL_PAGES
                    currentPages = rsp.currentPage
                    currentPages = Constants.Preferences.CURRENT_PAGE
                    for (dtc in rsp.dtcsList) {
                        val statusDtc = if (dtc.code == "CLEAN") {
                            2
                        } else if (dtc.deactivationWorkingHours == 0.0F) {
                            0
                        } else {
                            1
                        }
                        val act = "${dtc.activationWorkingHours}h"
                        val deact = if (dtc.deactivationWorkingHours != 0.0F) "${dtc.deactivationWorkingHours}h" else "---"

                        val route = DTC(dtc.id, dtc.code,act,deact, dtc.comments, statusDtc)
                        runOnMainThread {
                            routes.add(route)
                        }
                    }

                    observable.onNext(rsp.currentPage)
                }
            }
        }
    }

    fun getFreezeFrame(dtcID : Long) : Observable<DashboardAndStatus.DtcFreezeFrame>{
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.dashboardApi.getFreezeFrame(dtcID).execute().body() ?: return@executeAPI

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = resp.data.unpack(DashboardAndStatus.DtcFreezeFrame::class.java)
                    observable.onNext(rsp)
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }
}