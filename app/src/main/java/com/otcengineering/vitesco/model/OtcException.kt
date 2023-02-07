package com.otcengineering.vitesco.model

import com.otc.alice.api.model.Shared

class OtcException(val status: Shared.OTCStatus) : Exception()
