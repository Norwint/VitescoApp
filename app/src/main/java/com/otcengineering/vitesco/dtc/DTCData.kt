package com.otcengineering.vitesco.dtc

import com.otc.alice.api.model.DashboardAndStatus
import org.json.JSONObject

class DTCData {
    var dtcId = 0
    var activationDate = ""
    var deactivationDate = ""
    var comments = ""

    companion object {

        fun fromDTCHistory(dtc: DashboardAndStatus.Dtc): DTCData {
            val json = JSONObject(dtc.code)
            val dd = DTCData()

            dd.dtcId = json.getInt("")
            dd.activationDate = json.getString("")
            dd.deactivationDate = json.getString("")
            dd.comments = json.getString("")

            return dd
        }
    }
}