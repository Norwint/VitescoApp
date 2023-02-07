package com.otcengineering.vitesco.data

import androidx.databinding.BaseObservable

class DTC(
    var id : Long,
    var dtcId: String,
    var activationDate: String,
    var deactivationDate: String,
    var comments: String,
    var status: Int
) : BaseObservable() {
    fun getBackground() : Int {
        return when (status) {
            0 -> colorEnabled
            1 -> colorDisabled
            2 -> colorCleared
            else -> colorEnabled
        }
    }

    companion object {
        var colorEnabled: Int = 0
        var colorDisabled: Int = 0
        var colorCleared: Int = 0
    }

    fun onClickFreezeData() {

    }
}