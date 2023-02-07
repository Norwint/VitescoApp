package com.otcengineering.vitesco.data

import com.otc.alice.api.model.DashboardAndStatus.KnowYourBike
import com.otcengineering.vitesco.utils.instantFromServer
import com.otcengineering.vitesco.utils.parseLocal
import org.json.JSONObject
import java.time.Instant

class FiltersData{

    var engineOn = false
    var mil = false
    var warningLamp = false
    var bankLamp = false
    var powerOn = false
    var apsLevel = false
    var fuelLowLevel = false
    var engineOnWorkingHours = 0.0
    var milWorkingHours = 0.0
    var warningLampWorkingHours = 0.0
    var bankLampWorkingHours = 0.0
    var powerOnWorkingHours = 0.0
    var apsLevelWorkingHours = 0.0
    var fuelLowLevelWorkingHours = 0.0
    var engineOnDate = ""
    var milDate = ""
    var warningLampDate = ""
    var bankLampDate = ""
    var powerOnDate = ""
    var apsLevelDate = ""
    var fuelLowLevelDate = ""


    companion object {

        fun fromKnowYourBike(kyb: KnowYourBike): FiltersData {
            val json = JSONObject(kyb.statusData)
            val dd = FiltersData()

            dd.engineOn = json.getBoolean("engineOn")
            dd.mil = json.getBoolean("mil")
            dd.warningLamp = json.getBoolean("warningLamp")
            dd.bankLamp = json.getBoolean("bankLamp")
            dd.powerOn = json.getBoolean("powerOn")
            dd.apsLevel = json.getBoolean("apsLevel")
            dd.fuelLowLevel = json.getBoolean("fuelLowLevel")

            dd.engineOnWorkingHours = json.getDouble("engineOnWorkingHours")
            dd.milWorkingHours = json.getDouble("milWorkingHours")
            dd.warningLampWorkingHours = json.getDouble("warningLampWorkingHours")
            dd.bankLampWorkingHours = json.getDouble("bankLampWorkingHours")
            dd.powerOnWorkingHours = json.getDouble("powerOnWorkingHours")
            dd.apsLevelWorkingHours = json.getDouble("apsLevelWorkingHours")
            dd.fuelLowLevelWorkingHours = json.getDouble("fuelLowLevelWorkingHours")

            dd.engineOnDate = json.getString("engineOnWorkingHours")
            dd.milDate = json.getString("mil")
            dd.warningLampDate = json.getString("warningLamp")
            dd.bankLampDate = json.getString("bankLamp")
            dd.powerOnDate = json.getString("powerOn")
            dd.apsLevelDate = json.getString("apsLevel")
            dd.fuelLowLevelDate = json.getString("fuelLowLevel")

            return dd
        }

    }
//        val EMPTY = FiltersData(null, null, null, null, null, null, null, null)
//        private const val format = "dd/MM/yyyy - HH:mm:ss"
//
//        fun fromKnowYourBike(knowYourBike: KnowYourBike): FiltersData {
//            val json = JSONObject(knowYourBike.lampData)
//
//            return FiltersData(
//                (json.getString("airFilterOnDate")),
//                instantFromServer(json.getString("airFilterOffDate")),
//                instantFromServer(json.getString("oilEngineOnDate")),
//                instantFromServer(json.getString("oilEngineOffDate")),
//                instantFromServer(json.getString("oilFilterOnDate")),
//                instantFromServer(json.getString("oilFilterOffDate")),
//                instantFromServer(json.getString("fuelFilterOnDate")),
//                instantFromServer(json.getString("fuelFilterOffDate")),
//                json.optDouble("airFilterOn", 0.0).toFloat(),
//                json.optDouble("airFilterOff", 0.0).toFloat(),
//                json.optDouble("oilEngineOn", 0.0).toFloat(),
//                json.optDouble("oilEngineOff", 0.0).toFloat(),
//                json.optDouble("oilFilterOn", 0.0).toFloat(),
//                json.optDouble("oilFilterOff", 0.0).toFloat(),
//                json.optDouble("fuelFilterOn", 0.0).toFloat(),
//                json.optDouble("fuelFilterOff", 0.0).toFloat()
//            )
//        }
//    }

//    fun airHasError(): Boolean {
//        if (airFilterOn == null) {
//            return false
//        }
//
//        if (airFilterOff == null) return true
//
//        if (airFilterOn!! > airFilterOff) return true
//
//        return false
//    }
//
//    fun getParsedAir(): String {
//        return if (airHasError()) airFilterOn?.parseLocal(format) ?: "" else airFilterOff?.parseLocal(format) ?: ""
//    }
//
//    fun getAirWH(): Float = if (airHasError()) airFilterWorkingHoursOn else airFilterWorkingHoursOff
//
//    fun engineHasError(): Boolean {
//        if (engineOn == null) {
//            return false
//        }
//
//        if (engineOff == null) return true
//
//        if (engineOn!! > engineOff) return true
//
//        return false
//    }
//
//    fun getParsedEngine(): String {
//        return if (engineHasError()) engineOn?.parseLocal(format) ?: "" else engineOff?.parseLocal(format) ?: ""
//    }
//
//    fun getEngineWH(): Float = if (engineHasError()) engineWorkingHoursOn else engineWorkingHoursOff
//
//    fun oilFilterHasError(): Boolean {
//        if (oilFilterOn == null) {
//            return false
//        }
//
//        if (oilFilterOff == null) return true
//
//        if (oilFilterOn!! > oilFilterOff) return true
//
//        return false
//    }
//
//    fun getParsedOilFilter(): String {
//        return if (oilFilterHasError()) oilFilterOn?.parseLocal(format) ?: "" else oilFilterOff?.parseLocal(format) ?: ""
//    }
//
//    fun getOilWH(): Float = if (oilFilterHasError()) oilFilterWorkingHoursOn else oilFilterWorkingHoursOff
//
//    fun fuelFilterHasError(): Boolean {
//        if (fuelFilterOn == null) {
//            return false
//        }
//
//        if (fuelFilterOff == null) return true
//
//        if (fuelFilterOn!! > fuelFilterOff) return true
//
//        return false
//    }
//
//    fun getParsedFuelFilter(): String {
//        return if (fuelFilterHasError()) fuelFilterOn?.parseLocal(format) ?: "" else fuelFilterOff?.parseLocal(format) ?: ""
//    }
//
//    fun getFuelWH(): Float = if (fuelFilterHasError()) fuelFilterWorkingHoursOn else fuelFilterWorkingHoursOff
}