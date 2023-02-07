package com.otcengineering.vitesco.dtc

import com.otc.alice.api.model.DashboardAndStatus
import com.otcengineering.vitesco.data.FiltersData
import com.otcengineering.vitesco.utils.instantFromServer
import org.json.JSONObject
import java.time.Instant

class FiltersDTC (
    var enabled: Instant?,
    var disabled: Instant?,
    var cleared: Instant?,
) {
}