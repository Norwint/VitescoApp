package com.otcengineering.vitesco.model

import com.otc.alice.api.model.DashboardAndStatus
import com.otc.alice.api.model.DashboardAndStatus.KnowYourBike
import com.otc.alice.api.model.DashboardAndStatus.VehiclePart
import com.otc.alice.api.model.Shared
import com.otcengineering.vitesco.data.DashboardData
import com.otcengineering.vitesco.data.DashboardDataJoin
import com.otcengineering.vitesco.data.FiltersData
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.executeAPI
import com.otcengineering.vitesco.utils.toJSON
import io.reactivex.rxjava3.core.Observable
import java.io.IOException

class DashboardViewModel {
    fun getDashboardData(): Observable<DashboardDataJoin> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.dashboardApi.knowYourBike(Common.sharedPreferences.getString(
                    Constants.Preferences.VEHICLE_ID).toLong()).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null"))
                    return@executeAPI
                }

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    val rsp = resp.data.unpack(KnowYourBike::class.java)
                    val dd = DashboardData.fromKnowYourBike(rsp)
                    val filters = FiltersData.fromKnowYourBike(rsp)

                    observable.onNext(DashboardDataJoin(dd, filters))
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }

    private fun processOil(on: String?): String {
        if (on == null || on == "null") return "Ok"

        return "Low"
    }

    private fun processPower(on: String?, off: String?): String {
        if ((on == null || on == "null") && (off == null || off == "null")) return "---"

        if (off != null && off != "null") {
            return "Off"
        }

        return "On"
    }

    fun notifyChange(variable: VehiclePart, vehicleID: Long, hoursOfOperation: Float): Observable<Unit> {
        return Observable.create { observable ->
            executeAPI(observable) {
                val resp = Common.network.dashboardApi.putPartReplacement(
                    DashboardAndStatus.VehiclePartReplacement.newBuilder()
                        .setPart(variable)
                        .setEngineWorkingHours(hoursOfOperation)
                        .setVehicleId(vehicleID)
                        .build()
                ).execute().body()
                if (resp == null) {
                    observable.onError(IOException("Response is null"))
                    return@executeAPI
                }

                if (resp.status == Shared.OTCStatus.SUCCESS) {
                    observable.onNext(Unit)
                } else {
                    observable.onError(OtcException(resp.status))
                }
            }
        }
    }
}