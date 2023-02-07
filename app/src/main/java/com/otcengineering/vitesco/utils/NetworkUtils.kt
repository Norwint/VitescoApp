package com.otcengineering.vitesco.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter


private var handlerThread = HandlerThread("backgroundThread")
fun executeAPI(observable: ObservableEmitter<*>?, callback: () -> Unit) {
    if (!handlerThread.isAlive) {
        handlerThread.start()
    }
    val looper = handlerThread.looper
    val handler = Handler(looper)
    handler.post {
        try {
            callback()
        } catch (exc: Exception) {
            observable?.onError(exc)
            Log.e(handlerThread.name, "Exception:", exc)
        }
    }
}

fun getIMEI(): String {
    return "000000000000006"
}