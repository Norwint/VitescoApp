package com.otcengineering.vitesco.data

import com.otc.alice.api.model.DashboardAndStatus.KnowYourBike
import com.otcengineering.otcble.utils.DateUtils
import org.json.JSONObject

class DashboardData {

    var startStop = false

    var latitude = 0.0
    var longitude = 0.0
    var date = ""

    var batteryVoltage = 0.0
    var distanceAccumulation = 0.0
    var rpm = 0.0
    var distanceBasedFuelConsumption = 0.0
    var fuelConsumption = 0.0
    var fuelBlend = 0.0
    var throttlePositionSensor = 0.0
    var speed = 0.0
    var engineHours = 0.0
    var speedScoreLastTracklog = 0.0
    var timeScoreLastTracklog = 0.0
    var throttleScoreLastTracklog = 0.0
    var consumptionScoreLastTracklog = 0.0
    var rpmScoreLastTracklog = 0.0
    var ecoDrivingScoreLastTracklog = 0.0
    var drivingScoreLastTracklog = 0.0
    var distanceLastTracklog = 0.0
    var fuelConsumptionLastTracklog = 0.0
    var ecoLed = false

    var scoreAvg = 0.0
    var scoreEcoAvg = 0.0
    var rpmScoreAvg = 0.0
    var timeScoreAvg = 0.0
    var speedScoreAvg = 0.0
    var consumeScoreAvg = 0.0
    var throttleScoreAvg = 0.0


    companion object {
        fun fromKnowYourBike(kyb: KnowYourBike): DashboardData {
            val json = JSONObject(kyb.liveData)
            val dd = DashboardData()

            dd.speed = json.getDouble("speed")
            dd.batteryVoltage = json.getDouble("batteryVoltage")
            dd.distanceAccumulation = json.getDouble("distanceAccumulation")
            dd.rpm = json.getDouble("rpm")
            dd.distanceBasedFuelConsumption = json.getDouble("distanceBasedFuelConsumption")
            dd.fuelConsumption = json.getDouble("fuelConsumption")
            dd.fuelBlend = json.getDouble("fuelBlend")
            dd.throttlePositionSensor = json.getDouble("throttlePositionSensor")
            dd.engineHours = json.getDouble("engineHours")
            dd.speedScoreLastTracklog = json.getDouble("speedScoreLastTracklog")
            dd.timeScoreLastTracklog = json.getDouble("timeScoreLastTracklog")
            dd.throttleScoreLastTracklog = json.getDouble("throttleScoreLastTracklog")
            dd.consumptionScoreLastTracklog = json.getDouble("consumptionScoreLastTracklog")
            dd.rpmScoreLastTracklog = json.getDouble("rpmScoreLastTracklog")
            dd.ecoDrivingScoreLastTracklog = json.getDouble("ecoDrivingScoreLastTracklog")
            dd.fuelConsumptionLastTracklog = json.getDouble("fuelConsumptionLastTracklog")
            dd.distanceLastTracklog = json.getDouble("distanceLastTracklog")
            dd.drivingScoreLastTracklog = json.getDouble("drivingScoreLastTracklog")
            dd.ecoLed = json.getBoolean("ecoLed")

            if(kyb.lastRouteScoreAverageData == "") {
                dd.scoreAvg = 0.0
                dd.rpmScoreAvg = 0.0
                dd.timeScoreAvg = 0.0
                dd.speedScoreAvg = 0.0
                dd.consumeScoreAvg = 0.0
                dd.throttleScoreAvg = 0.0
            } else {
                val jsonAvg = JSONObject(kyb.lastRouteScoreAverageData)

                dd.scoreAvg = jsonAvg.getDouble("scoreAvg")
                dd.rpmScoreAvg = jsonAvg.getDouble("rpmScoreAvg")
                dd.timeScoreAvg = jsonAvg.getDouble("timeScoreAvg")
                dd.speedScoreAvg = jsonAvg.getDouble("speedScoreAvg")
                dd.consumeScoreAvg = jsonAvg.getDouble("consumeScoreAvg")
                dd.throttleScoreAvg = jsonAvg.getDouble("throttleScoreAvg")

            }


            dd.scoreEcoAvg = kyb.lastRouteEcoDrivingScoreAverage.toDouble()

            dd.date = DateUtils.serverDateParser(json.getString("date"), "dd/MM/yyyy - HH:mm:ss")

            dd.latitude = json.getDouble("latitude")
            dd.longitude = json.getDouble("longitude")

            return dd
        }
    }
}