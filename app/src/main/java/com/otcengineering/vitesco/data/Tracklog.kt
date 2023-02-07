package com.otcengineering.vitesco.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import com.otc.alice.api.model.MyTrip
import com.otcengineering.otcble.utils.DateUtils
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.model.TracklogViewModel
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.EntrySet
import com.otcengineering.vitesco.utils.getDrawable
import com.ultramegasoft.radarchart.RadarHolder
import java.util.*


class Tracklog(
    var ctx: Context,
    var startingDate: String,
    var endingDate: String,
    var totalTime: String,
    var tracklogID: Long = 0,
    var ecoDrivingScore: Float,
    var drivingScore: Float,
    var speedScore: Float,
    var timeScore: Float,
    var throttleScore: Float,
    var consumeScore: Float,
    var rpmScore: Float,
    var drivingScoreAvg: Float,
    var ecoDrivingScoreAvg: Float,
    var speedScoreAvg: Float,
    var timeScoreAvg: Float,
    var throttleScoreAvg: Float,
    var consumeScoreAvg: Float,
    var rpmScoreAvg: Float,
    var fuelConsumption: String,
    var distance: Int,
    var date: String,
    var duration: Int,
) : BaseObservable() {

    var expanded = false
    var selected = false
    private var folded = true
    private var foldedTech = true
    private var foldedData = true

    private lateinit var newArrayListScore: ArrayList<ListItemScore>

    private val viewModel: TracklogViewModel by lazy {
        TracklogViewModel()
    }

    companion object {

        val colors: IntArray by lazy {
            intArrayOf(
                Common.colorBlue, Common.colorGreen, Common.colorOrange
            )
        }

        fun getItems(): Array<String> {
            val array = Array(18) { idx -> "" }

            for (i in 0..17) {
                val item = MyTrip.BodyVariable.forNumber(i)
                val name = item.name
                array[i] = name
            }

            return array
        }
    }

    fun dataTracklog() : String{
        return DateUtils.serverDateParser(date, "dd/MM/yyyy - HH:mm:ss")
    }

    fun distanceKm() : String {
        return "$distance Km"
    }

    fun durationTime() : String {

        val timeSec: Int = duration

        val hours = timeSec / 3600
        var temp = timeSec - hours * 3600
        val mins = temp / 60
        temp -= mins * 60
        val secs = temp

        return if (hours == 0) {
            return "$mins min $secs sec"
        } else if (hours == 0 && mins == 0) {
            "$secs sec"
        } else {
            "$hours h $mins min $secs sec"
        }

    }

    fun consumptionFuel() : String {
        return "($fuelConsumption)"
    }

    fun setExpansion(exp: Boolean) {
        expanded = exp
        if (!exp) setSelection(false)
        notifyChange()
    }

    var graphData = ArrayList<EntrySet>()
    var graphLabel = ""

    init {
        graph(0)
    }

    @SuppressLint("CheckResult")
    fun graph(position: Int) {
        graphLabel = getItems()[position]

        viewModel.getDataOfVariable(graphLabel, tracklogID).subscribe({ bodyVar ->
            graphData.clear()

            for (i in 0 until bodyVar.seriesList.size) {
                val set = bodyVar.seriesList[i]
                val values: ArrayList<Float> = ArrayList()
                for (value in set.valuesList) {
                    values.add(value.toFloat())
                }
                graphData.add(
                    EntrySet(
                        values,
                        colors[i],
                        set.name
                    )
                )
            }

            notifyChange()


        }, {})
    }

    fun getColorEco(): Int {
        return if (ecoDrivingScore*10 >= 80) {
            ContextCompat.getColor(ctx,R.color.colorLightGreen)
        } else if (ecoDrivingScore.toInt()*10 in 60..79){
            ContextCompat.getColor(ctx,R.color.colorYellow)
        } else if (ecoDrivingScore.toInt()*10 in 40..59){
            ContextCompat.getColor(ctx,R.color.colorOrange)
        } else {
            ContextCompat.getColor(ctx,R.color.colorRed)
        }
    }

    fun getColor(): Int {

        val drivingScor = drivingScore.toDouble()*10

        return if (drivingScor >= 80) {
            ContextCompat.getColor(ctx,R.color.colorLightGreen)
        } else if (drivingScor.toInt() in 60..79){
            ContextCompat.getColor(ctx,R.color.colorYellow)
        } else if (drivingScor.toInt() in 40..59){
            ContextCompat.getColor(ctx,R.color.colorOrange)
        } else {
            ContextCompat.getColor(ctx,R.color.colorRed)
        }
    }

    fun avgData() : ArrayList<RadarHolder> {
        val mData = ArrayList<RadarHolder>()

        mData.add(RadarHolder("Speed", (speedScoreAvg*10).toInt()))
        mData.add(RadarHolder("Time", (timeScoreAvg*10).toInt()))
        mData.add(RadarHolder("Throttle", (throttleScoreAvg*10).toInt()))
        mData.add(RadarHolder("Consume", (consumeScoreAvg*10).toInt()))
        mData.add(RadarHolder("RPM", (rpmScoreAvg*10).toInt()))

        return mData
    }

    fun scoreData() : ArrayList<RadarHolder> {
        val mData = ArrayList<RadarHolder>()

        mData.add(RadarHolder("Speed", (speedScore*10).toInt()))
        mData.add(RadarHolder("Time", (timeScore*10).toInt()))
        mData.add(RadarHolder("Throttle", (throttleScore*10).toInt()))
        mData.add(RadarHolder("Consume", (consumeScore*10).toInt()))
        mData.add(RadarHolder("RPM", (rpmScore*10).toInt()))

        return mData
    }

    fun scoreBkg() : ArrayList<RadarHolder> {
        val mData = ArrayList<RadarHolder>()

        mData.add(RadarHolder("Speed", 0))
        mData.add(RadarHolder("Time", 0))
        mData.add(RadarHolder("Throttle", 0))
        mData.add(RadarHolder("Consume", 0))
        mData.add(RadarHolder("RPM", 0))

        return mData
    }

    fun maxRadar() : Int {
        return 100
    }

    fun maxRadarBkg() : Int {
        return 10
    }

    fun circleColorEco() : Int {
        return ContextCompat.getColor(ctx,R.color.transparent)
    }

    fun circlePolygonEco() : Int {
        return ContextCompat.getColor(ctx,R.color.black)
    }

    fun circleLabelEco() : Int {
        return ContextCompat.getColor(ctx,R.color.transparent)
    }

    fun circleColor() : Int {
        return ContextCompat.getColor(ctx,R.color.colorPrimaryProgress)
    }

    fun circlePolygon() : Int {
        return ContextCompat.getColor(ctx,R.color.check_button)
    }

    fun getArray() : ArrayList<ListItemScore>{
        newArrayListScore = arrayListOf()

        val title = arrayOf(
            ctx.getString(R.string.speed),
            ctx.getString(R.string.acceleration),
            ctx.getString(R.string.throttle),
            ctx.getString(R.string.breakEvents),
            ctx.getString(R.string.rpm),
        )

        val vals = arrayOf(
            String.format(Locale.US, "%01.01f", speedScore),
            String.format(Locale.US, "%01.01f", timeScore),
            String.format(Locale.US, "%01.01f", throttleScore),
            String.format(Locale.US, "%01.01f", consumeScore),
            String.format(Locale.US, "%01.01f", rpmScore),
        )

        val background = arrayOf(
            setBackgroundScore(speedScore.toInt()),
            setBackgroundScore(timeScore.toInt()),
            setBackgroundScore(throttleScore.toInt()),
            setBackgroundScore(consumeScore.toInt()),
            setBackgroundScore(rpmScore.toInt()),
        )

        val icons = arrayOf(
            if(speedScore < speedScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(timeScore < timeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(throttleScore < throttleScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(consumeScore < consumeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(rpmScore < rpmScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
        )

        val average = arrayOf(
            ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", speedScoreAvg)),
            ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", timeScoreAvg)),
            ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", throttleScoreAvg)),
            ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", consumeScoreAvg)),
            ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", rpmScoreAvg)),
        )

        val visibility = arrayOf(
            true,
            true,
            true,
            true,
            true,
        )

        for (i in title.indices){
            val items = ListItemScore(title[i], vals[i], icons[i].getDrawable(ctx)!!, background[i], average[i], visibility[i])
            newArrayListScore.add(items)
        }
        return newArrayListScore
    }

    fun setBackgroundScore(score: Int) : Drawable {
        if (score >= 8) {
            return ctx.resources.getDrawable(R.color.colorLightGreen)
        } else if (score < 8 && score >= 6) {
            return ctx.resources.getDrawable(R.color.colorYellow)
        } else if (score < 6 && score >= 4) {
            return ctx.resources.getDrawable(R.color.colorOrange)
        } else {
            return ctx.resources.getDrawable(R.color.colorRed)
        }
    }

    fun toggleSelected() {
        selected = !selected
        notifyChange()
    }

    fun setSelection(sel: Boolean) {
        selected = sel
        notifyChange()
    }

    fun getStartDate() = startingDate
    fun getEndDate() = endingDate

    fun foldable() {
        folded = !folded
        notifyChange()
    }

    fun foldableTech() {
        foldedTech = !foldedTech
        notifyChange()
    }

    fun foldableData() {
        foldedData = !foldedData
        notifyChange()
    }
    fun drivingScore() : String{
        return String.format(Locale.US, "%01.01f", drivingScore.toFloat())
    }
    fun drivingScoreProgress() : Float{
        return (drivingScore*10)
    }
    fun ecoDrivingScoreProgress() : Float{
        return (ecoDrivingScore*10).toFloat()
    }
    fun rotationEco() : Int {
        return if (ecoDrivingScore >= ecoDrivingScoreAvg) 180 else 0
    }
    fun rotationDriving() : Int {
        return if (drivingScore >= drivingScoreAvg) 180 else 0
    }
    fun ecoDrivingScore() : String{
        return String.format(Locale.US, "%01.01f", ecoDrivingScore.toFloat())
//        return String.format(Locale.US, "%01.01f", ecoDrivingScore)
    }
    fun ecoDrivingScoreAvg() : String{
        return  ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", ecoDrivingScoreAvg))
    }
    fun drivingScoreAvg() : String{
        return  ctx.getString(R.string.average_title, String.format(Locale.US, "%01.01f", drivingScoreAvg))
    }
    fun getVisibility() : Int{
        return if (folded) View.GONE else View.VISIBLE
    }

    fun getVisibilityTech() : Int{
        return if (foldedTech) View.GONE else View.VISIBLE
    }

    fun getVisibilityData() : Int{
        return if (foldedData) View.GONE else View.VISIBLE
    }

    fun getVisibilityRotation() : Int{
        return if (folded) 0 else 180
    }

    fun getVisibilityRotationTech() : Int{
        return if (foldedTech) 0 else 180
    }
    fun getVisibilityRotationData() : Int{
        return if (foldedData) 0 else 180
    }

    val selectImage: Int
        get() = if (selected) R.drawable.ic_check else R.drawable.ic_trash
}