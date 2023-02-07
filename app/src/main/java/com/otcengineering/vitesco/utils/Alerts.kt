package com.otcengineering.vitesco.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.ListFilterDtc
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.otcengineering.vitesco.view.fragment.DTCFragment


fun Context.showAlertDialog(positiveButtonText: String, message: String, onPositiveClick: () -> Unit) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
    builder.setPositiveButton(positiveButtonText) { dialog, _ ->
        dialog.dismiss()
        onPositiveClick()
    }
    val alert: AlertDialog = builder.create()
    alert.show()
}

fun Context.showAlertDialogOk(message: String, onPositiveClick: () -> Unit) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
        dialog.dismiss()
        onPositiveClick()
    }
    val alert: AlertDialog = builder.create()
    alert.show()
}

fun Context.showAlertDialogFilter(inflater: LayoutInflater, onPositiveClick: (String, String) -> Unit) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    val dialogView: View = inflater.inflate(R.layout.menu_filter, null)
    builder.setView(dialogView)
    builder.setCancelable(false)

    val close = dialogView.findViewById<ImageView>(R.id.closeDialog)

    builder.setPositiveButton("APPLY") { dialog, _ ->
        val filter = dialogView.findViewById<EditText>(R.id.dtcCode)
        val filterText = filter.text.toString()

        val enabled = dialogView.findViewById<RadioButton>(R.id.enabled)
        val disabled = dialogView.findViewById<RadioButton>(R.id.disabled)

        val selected = if (enabled.isChecked) {
            "activated"
        } else if (disabled.isChecked) {
            "deactivated"
        } else {
            "all"
        }

        dialog.dismiss()
        onPositiveClick(selected, filterText.ifEmpty { "-1" })
    }
    val alert: AlertDialog = builder.create()
    alert.show()

    close.setOnClickListener {
        alert.dismiss()
    }
}