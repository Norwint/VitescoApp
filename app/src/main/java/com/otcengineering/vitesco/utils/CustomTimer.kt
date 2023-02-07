package com.otcengineering.vitesco.utils

import java.util.Timer
import java.util.TimerTask

class CustomTimer: Timer() {
    fun createAndRun(delay: Long, repeats: Long, func: () -> Unit) {
        scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                func()
            }
        }, delay, repeats)
    }
}