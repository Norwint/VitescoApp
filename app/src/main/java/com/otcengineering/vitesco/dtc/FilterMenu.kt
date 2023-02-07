package com.otcengineering.vitesco.dtc


import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.otcengineering.vitesco.databinding.MenuFilterBinding

class FilterMenu(context: Context) : AlertDialog(context, android.R.style.Theme_Light) {

    private val binding: MenuFilterBinding by lazy {
        MenuFilterBinding.inflate(layoutInflater)
    }

    private fun showOnMainThread() {
        Handler(Looper.getMainLooper()).post {
            show()
        }
    }

    fun filters () : FilterMenu{
        showOnMainThread()

        return this
    }
}