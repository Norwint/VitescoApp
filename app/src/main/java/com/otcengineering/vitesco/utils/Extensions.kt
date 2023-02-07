package com.otcengineering.vitesco.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.ListItemScore
import com.otcengineering.vitesco.data.Ranking
import com.otcengineering.vitesco.data.Tracklog
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.ultramegasoft.radarchart.RadarHolder
import com.ultramegasoft.radarchart.RadarView
import id.ionbit.ionalert.IonAlert
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

@BindingAdapter("android:chartLabel")
fun setChartLabel(view: LineChart, label: String) {
    try {
        view.data.dataSets.first().label = label
    } catch (e: Exception) {

    }
}

class EntrySet(
    val data: List<Float>,
    val color: Int,
    val label: String
)

@BindingAdapter("android:data")
fun setData(view: LineChart, data: List<EntrySet>) {
    val entrySets = ArrayList<LineDataSet>()

    for (set in data) {
        val entries = mutableListOf<Entry>()
        for (i in set.data.indices) {
            entries.add(Entry(i.toFloat(), set.data[i]))
        }

        val dataSet = LineDataSet(entries, set.label)
        dataSet.color = set.color
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2.0F
        entrySets.add(dataSet)
    }

    val lineData = LineData(entrySets.toList())

    view.data = lineData
    view.invalidate()
}

@BindingAdapter("srcResId")
fun ImageView.setSrcResId(resId: Int) {
    setImageDrawable(ContextCompat.getDrawable(context, resId))
}

@BindingAdapter("progress_color")
fun CircularProgressBar.ecoColor(color: Int) {
    progressBarColor = color
}

@BindingAdapter("radar_add")
fun RadarView.radarMax(max: Int) {
    maxValue = max
}

@BindingAdapter("radar_circle_color")
fun RadarView.radarCircleColor(color: Int) {
    circleColor = color
}

@BindingAdapter("radar_polygon_color")
fun RadarView.radarPolygonColor(color: Int) {
    polygonColor = color
}

@BindingAdapter("radar_label_color")
fun RadarView.labelColor(color: Int) {
    labelColor = color
}

@BindingAdapter("radar_data")
fun RadarView.radarData(radar: java.util.ArrayList<RadarHolder>) {
    this.setData(radar)
}

@BindingAdapter("data_recycler")
fun RecyclerView.dataRecycler(array: java.util.ArrayList<ListItemScore>) {
    val recyclerViewScore : RecyclerViewAdapter<ListItemScore> = RecyclerViewAdapter(R.layout.row_drivingscore)
    this.adapter = recyclerViewScore
    recyclerViewScore.setList(array)

}

@BindingAdapter("data_recycler_ranking")
fun RecyclerView.dataRecyclerRanking(array: java.util.ArrayList<Ranking>) {
    val recyclerViewScore : RecyclerViewAdapter<Ranking> = RecyclerViewAdapter(R.layout.row_ranking)
    this.adapter = recyclerViewScore
    recyclerViewScore.setList(array)

}

@BindingAdapter("entries")
fun AppCompatSpinner.entries(array: Array<String>) {
    val adapter = ArrayAdapter<String>(
        context, android.R.layout.simple_spinner_item, array
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    this.adapter = adapter
}

fun runOnMainThread(function: () -> Unit) {
    if (Thread.currentThread() == Looper.getMainLooper().thread) {
        try {
            function()
        } catch (ex: Exception) {
            Log.e("RunOnMainThread", "", ex)
        }
    } else {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            try {
                function()
            } catch (ex: Exception) {
                Log.e("RunOnMainThread", "", ex)
            }
        }
    }
}

fun <T: AppCompatActivity> Context.startActivity(activityClass: Class<T>) {
    startActivity(Intent(this, activityClass))
}

private var loadingPopup: IonAlert? = null
fun Context.showLoading() {
    runOnMainThread {
        if (loadingPopup != null) {
            dismissLoading()
        }
        loadingPopup = IonAlert(this, IonAlert.PROGRESS_TYPE)
            .showCancelButton(false)
            .setSpinKit("FadingCircle")
        loadingPopup?.show()
    }
}

fun Context.isLoadingShown() = loadingPopup != null

fun Context.dismissLoading() {
    runOnMainThread {
        loadingPopup?.dismissWithAnimation()
        loadingPopup = null
    }
}

fun runDelayed(delay: Long, callback: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        callback()
    }, delay)
}

fun Int.getDrawable(ctx: Context): Drawable? = ContextCompat.getDrawable(ctx, this)

fun Instant.parseLocal(fmt: String): String {
    val fmtr = DateTimeFormatter.ofPattern(fmt)
    val offset = this.atZone(ZoneId.systemDefault())
    return offset.format(fmtr)
}

fun instantFromServer(dateServer: String?): Instant? {
    if (dateServer == null || dateServer == "null") return null

    return LocalDateTime.parse(dateServer, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .toInstant(ZoneOffset.UTC)
}

fun Context.toast(message: String) {
    runOnMainThread {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

fun String.toJSON(): JSONObject {
    return JSONObject(this)
}


fun ByteArray.parseToString(): String {
    var result = ""
    var delim = ""
    for (i in indices) {
        result += delim
        result += String.format(Locale.US, "%02X", this[i])
        delim = " "
    }
    return result
}