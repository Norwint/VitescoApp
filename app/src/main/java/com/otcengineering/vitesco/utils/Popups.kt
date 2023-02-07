package com.otcengineering.vitesco.utils

import android.content.Context
import id.ionbit.ionalert.IonAlert

object Popups {
    fun showErrorPopup(ctx: Context, title: String, status: String) {
        runOnMainThread {
            val alert = IonAlert(ctx, IonAlert.ERROR_TYPE)
            alert.titleText = title
            alert.contentText = status
            alert.confirmText = "Ok"
            alert.show()
        }
    }
}