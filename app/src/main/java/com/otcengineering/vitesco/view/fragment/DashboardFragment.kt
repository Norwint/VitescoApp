package com.otcengineering.vitesco.view.fragment

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.otcengineering.otcble.BleSDK
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.*
import com.otcengineering.vitesco.databinding.FragmentDashboardBinding
import com.otcengineering.vitesco.model.DashboardViewModel
import com.otcengineering.vitesco.model.PushNotificationViewModel
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.getDrawable
import com.otcengineering.vitesco.utils.runOnMainThread
import com.otcengineering.vitesco.view.activity.RankingActivity
import com.otcengineering.vitesco.view.activity.StatusActivity
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.ultramegasoft.radarchart.RadarHolder
import java.util.*


class DashboardFragment : Fragment() {
    private lateinit var recyclerView : RecyclerViewAdapter<ListItemIcon>
    private lateinit var recyclerViewScore : RecyclerViewAdapter<ListItemScore>
    private lateinit var recyclerFilters : RecyclerViewAdapter<DashboardFilters>
    private lateinit var newArrayList: ArrayList<ListItemIcon>
    private lateinit var newArrayListScore: ArrayList<ListItemScore>
    private var filtersList = ArrayList<DashboardFilters>()
    private var mData = ArrayList<RadarHolder>()
    private var mDataAverage = ArrayList<RadarHolder>()
    private var mDataBackground = ArrayList<RadarHolder>()

    private var timer = Timer()
    private var bleTimer = Timer()

    private val binding: FragmentDashboardBinding by lazy {
        FragmentDashboardBinding.inflate(
            layoutInflater
        )
    }

    private val viewModel: DashboardViewModel by lazy { DashboardViewModel() }

    var lastUpdate = "---"
    var hoursOperation = "---"
    var engineStatus = "---"
    var oilPressure = "---"
    var workingHours = 0.0F
    private var coordinatesString = "---"
    var lastData = "---"
    var lastMessage = "---"

    var rotationEco = 0
    var rotationDriving = 0

    var fuelLevel = 0

    private var faults = false
    private var lowOil = false

    fun conditionIcon() = ContextCompat.getDrawable(requireContext(), if (faults) R.drawable.ic_baseline_close else R.drawable.ic_baseline_check)
    fun oilIcon() = ContextCompat.getDrawable(requireContext(), if (lowOil) R.drawable.ic_baseline_close else R.drawable.ic_baseline_check)
    fun oilColor() = ContextCompat.getColor(requireContext(), if (lowOil) R.color.colorRed else R.color.black)

//    private fun processData(filtersData: FiltersData, engineData: String, oilData: String) {
//        filterInitialize(filtersData)
//        faults = (filtersData.oilFilterHasError() || filtersData.airHasError() || filtersData.fuelFilterHasError() || filtersData.engineHasError())
//        engineStatus = engineData
//        oilPressure = oilData
//        lowOil = oilData != "Ok"
//
//        runOnMainThread {
//            recyclerFilters.setList(filtersList)
//        }
//    }

    private fun updateData() {

        viewModel.getDashboardData().subscribe({ dd ->
            coordinatesString = "${dd.dashboardData.latitude}, ${dd.dashboardData.longitude}"
            if (!BleSDK.isConnected()) {
                runOnMainThread {
                    dataInitialize(dd.dashboardData)
                    dataInitializeScore(dd.dashboardData)
                    dataFilterInitialize(dd.filtersData)
                    detectedBLE(false)
                }
            } else {
                val status = BluetoothService.getService(requireContext()).getData()
                dd.filtersData.engineOn = status.startStop
                runOnMainThread {
                    dataInitialize(status)
                    dataInitializeScoreBLE(status)
                    dataFilterInitialize(dd.filtersData)
                    detectedBLE(true)
                }
            }
//            processData(dd.filtersData, dd.engineData, dd.oilData)
            binding.invalidateAll()
        }, {
//            processData(FiltersData.EMPTY, "", "")
            dataInitialize(DashboardData())
            dataInitializeScore(DashboardData())
            dataFilterInitialize(FiltersData())
            binding.invalidateAll()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this

        recyclerView = RecyclerViewAdapter(R.layout.row_dashboard)
        recyclerViewScore = RecyclerViewAdapter(R.layout.row_drivingscore)

        binding.recyclerView.adapter = recyclerView

        binding.recyclerViewScore.adapter = recyclerViewScore

        binding.scoredata.visibility = View.GONE

        DashboardFilters.detectedString = getString(R.string.detected_x)
        DashboardFilters.replacedString = getString(R.string.replaced_x)

        DashboardFilters.okIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_check)
        DashboardFilters.errorIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_close)
        DashboardFilters.okColor = ContextCompat.getColor(requireContext(), R.color.colorGreen)
        DashboardFilters.errorColor = ContextCompat.getColor(requireContext(), R.color.colorRed)

        binding.radar.maxValue = 100
        binding.radar2.maxValue = 100
        binding.radar3.maxValue = 10
        binding.radar.circleColor = ContextCompat.getColor(requireContext(),R.color.transparent)
        binding.radar2.circleColor = ContextCompat.getColor(requireContext(),R.color.transparent)
        binding.radar3.circleColor = ContextCompat.getColor(requireContext(),R.color.colorPrimaryProgress)
        binding.radar.polygonColor = ContextCompat.getColor(requireContext(),R.color.check_button)
        binding.radar2.polygonColor = ContextCompat.getColor(requireContext(),R.color.black)
        binding.radar.labelColor = ContextCompat.getColor(requireContext(),R.color.transparent)
        binding.radar2.labelColor = ContextCompat.getColor(requireContext(),R.color.transparent)

        binding.rankingImage.setOnClickListener {
            startActivity(Intent(requireContext(),RankingActivity::class.java))
        }

        binding.carConditionLayout.setOnClickListener {
            startActivity(Intent(requireContext(),StatusActivity::class.java))
        }

        updateData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask() {
            var times = 0
            override fun run() {
                if (BleSDK.isConnected() || times % 5 == 0) {
                    updateData()
                }
                times += 1
            }
        }, 5000, 5000)
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    fun goToCoordinates() {
        val gmmIntentUri = Uri.parse("geo:${coordinatesString}?q=${coordinatesString}(Uploading Location)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.resolveActivity(requireContext().packageManager)?.let {
            startActivity(mapIntent)
        }

    }

//    private fun filterInitialize(data: FiltersData) {
//        filtersList.clear()
//
//        filtersList.add(DashboardFilters(getString(R.string.air_filter), data.getParsedAir(),
//            data.getAirWH(), data.airHasError()) {
//            requireContext().showAlertDialog(getString(R.string.CLEAR), getString(R.string.question_reset_air_filter)) {
//                BluetoothService.getService(requireContext())
//                    .sendReset(BluetoothService.ResetBits.AirFilterReset)
//                viewModel.notifyChange(
//                    DashboardAndStatus.VehiclePart.AIR_FILTER, Common.sharedPreferences.getLong(
//                    Constants.Preferences.VEHICLE_ID, 0L
//                    ), workingHours
//                ).subscribe({}, {})
//            }
//        })
//        filtersList.add(DashboardFilters(getString(R.string.engine_oil), data.getParsedEngine(),
//            data.getEngineWH(), data.engineHasError()) {
//            requireContext().showAlertDialog(getString(R.string.CLEAR), getString(R.string.question_reset_engine_oil)) {
//                BluetoothService.getService(requireContext())
//                    .sendReset(BluetoothService.ResetBits.MaintenanceReset)
//                viewModel.notifyChange(
//                    DashboardAndStatus.VehiclePart.OIL_ENGINE, Common.sharedPreferences.getLong(
//                        Constants.Preferences.VEHICLE_ID, 0L
//                    ), workingHours
//                ).subscribe({}, {})
//            }
//        })
//        filtersList.add(DashboardFilters(getString(R.string.oil_filter), data.getParsedOilFilter(),
//            data.getOilWH(), data.oilFilterHasError()) {
//            requireContext().showAlertDialog(getString(R.string.CLEAR), getString(R.string.question_reset_oil_filter)) {
//                BluetoothService.getService(requireContext())
//                    .sendReset(BluetoothService.ResetBits.EngineOilReset)
//                viewModel.notifyChange(
//                    DashboardAndStatus.VehiclePart.OIL_FILTER, Common.sharedPreferences.getLong(
//                        Constants.Preferences.VEHICLE_ID, 0L
//                    ), workingHours
//                ).subscribe({}, {})
//            }
//        })
//        filtersList.add(DashboardFilters(getString(R.string.fuel_filter), data.getParsedFuelFilter(),
//            data.getFuelWH(), data.fuelFilterHasError()) {
//            requireContext().showAlertDialog(getString(R.string.CLEAR), getString(R.string.question_reset_fuel_filter)) {
//                BluetoothService.getService(requireContext())
//                    .sendReset(BluetoothService.ResetBits.FuelFilterReset)
//                viewModel.notifyChange(
//                    DashboardAndStatus.VehiclePart.FUEL_FILTER, Common.sharedPreferences.getLong(
//                        Constants.Preferences.VEHICLE_ID, 0L
//                    ), workingHours
//                ).subscribe({}, {})
//            }
//        })
//    }

    private fun dataFilterInitialize(data: FiltersData) {
        engineStatus = if(data.engineOn) "On" else "Off"

        faults = data.mil || data.warningLamp || data.bankLamp || data.apsLevel || data.fuelLowLevel

        binding.carCondition.visibility = View.VISIBLE

    }

    private fun convertWorkingHours(workingHours: Double): String {
        val hours = workingHours.toInt()
        val minutes = ((workingHours - hours.toDouble()) * 60.0).toInt()
        var txt = ""

        if (hours > 0) {
            txt = "${hours}h "
        }
        txt += "${minutes}min"
        return txt
    }

    private fun dataInitialize(data: DashboardData) {
        newArrayList = arrayListOf()

        hoursOperation = convertWorkingHours(data.engineHours)

        runOnMainThread {
            binding.ecoCondition.visibility = View.VISIBLE

            if(!data.ecoLed) {
                binding.ecoCondition.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorRed),
                    PorterDuff.Mode.MULTIPLY
                )
            } else {
                binding.ecoCondition.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorLightGreen),
                    PorterDuff.Mode.MULTIPLY
                )
            }
        }

        val title = arrayOf(
            getString(R.string.speed),
            //getString(R.string.RPM),
            getString(R.string.fuelConsumption),
            getString(R.string.batteryVoltage),
            getString(R.string.distanceAccumulation),
            getString(R.string.distanceBasedFuelCons),
            getString(R.string.fuelBlend),
            getString(R.string.throttlePositionSensor),
        )

        val vals = arrayOf(
            String.format(Locale.US, "%01.01f", data.speed.toFloat()) + " Km/h",
            //String.format(Locale.US, "%01.01f", data.rpm.toFloat()) + " RPM",
            String.format(Locale.US, "%01.01f", data.fuelConsumption.toFloat()) + " ml/100ms",
            String.format(Locale.US, "%01.01f", data.batteryVoltage.toFloat()) + " V",
            String.format(Locale.US, "%01.01f", data.distanceAccumulation.toFloat()) + " Km",
            String.format(Locale.US, "%01.01f", data.distanceBasedFuelConsumption.toFloat()) +" Km/l",
            String.format(Locale.US, "%01.01f", data.fuelBlend.toFloat()) + " %",
            String.format(Locale.US, "%01.01f", data.throttlePositionSensor.toFloat()) + " °TPS",
        )

        val icons = arrayOf(
            R.drawable.gas,
            //R.drawable.gauge,
            R.drawable.water,
            R.drawable.lighting,
            R.drawable.arrows,
            R.drawable.water_drop,
            R.drawable.valve,
            R.drawable.gas_pedal,
        )

        newArrayList.clear()
        for (i in title.indices){
            val items = ListItemIcon(
                title[i], vals[i],
                icons[i].getDrawable(requireContext())!!,
            )
            newArrayList.add(items)
        }
        runOnMainThread {
            recyclerView.setList(newArrayList)
            lastUpdate = getString(R.string.update, data.date)
            //lastData = "Trip " + " at " + data.date
        }

        PushNotificationViewModel().getNotification().subscribe {
            runOnMainThread {
                lastData = it
                binding.invalidateAll()
            }
        }
    }

    private fun dataInitializeScore(data: DashboardData) {

        if(data.ecoDrivingScoreLastTracklog == 0.0 && data.drivingScoreLastTracklog == 0.0) {
            binding.scoredata.visibility = View.GONE
        } else {
            binding.scoredata.visibility = View.VISIBLE
        }

        newArrayListScore = arrayListOf()
        mData.clear()
        mDataAverage.clear()
        mDataBackground.clear()

        rotationDriving = if (data.drivingScoreLastTracklog > data.scoreAvg) 180 else 0
        rotationEco = if (data.ecoDrivingScoreLastTracklog > data.scoreEcoAvg) 180 else 0

        mData.add(RadarHolder("Speed", (data.speedScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Time", (data.timeScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Throttle", (data.throttleScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Consume", (data.consumptionScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("RPM", (data.rpmScoreLastTracklog*10).toInt()))

        mDataAverage.add(RadarHolder("Speed", (data.speedScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Time", (data.timeScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Throttle", (data.throttleScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Consume", (data.consumeScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("RPM", (data.rpmScoreAvg*10).toInt()))

        mDataBackground.add(RadarHolder("Speed", 0))
        mDataBackground.add(RadarHolder("Time", 0))
        mDataBackground.add(RadarHolder("Throttle", 0))
        mDataBackground.add(RadarHolder("Consume", 0))
        mDataBackground.add(RadarHolder("RPM", 0))

        binding.progressEco.progress = data.ecoDrivingScoreLastTracklog.toFloat()*10
        binding.txtEco.text =  String.format(Locale.US, "%01.01f", (data.ecoDrivingScoreLastTracklog.toFloat()))
        binding.progressSafety.progress = data.drivingScoreLastTracklog.toFloat()*10
        binding.txtSafety.text = String.format(Locale.US, "%01.01f", (data.drivingScoreLastTracklog.toFloat()))

        binding.radar.setData(mDataAverage)
        binding.radar2.setData(mData)
        binding.radar3.setData(mDataBackground)

        binding.averageEco.text = getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.scoreEcoAvg))
        binding.averageDriving.text = getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.scoreAvg))

        if (data.drivingScoreLastTracklog >= 8) {
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorLightGreen)
        } else if (data.drivingScoreLastTracklog >= 6 && data.drivingScoreLastTracklog < 8){
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorYellow)
        } else if (data.drivingScoreLastTracklog >= 4 && data.drivingScoreLastTracklog < 6){
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorOrange)
        } else {
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorRed)
        }

        if (data.ecoDrivingScoreLastTracklog >= 8) {
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorLightGreen)
        } else if (data.ecoDrivingScoreLastTracklog >= 6 && data.ecoDrivingScoreLastTracklog < 8){
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorYellow)
        } else if (data.ecoDrivingScoreLastTracklog >= 4 && data.ecoDrivingScoreLastTracklog < 6){
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorOrange)
        } else {
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorRed)
        }

        val title = arrayOf(
            getString(R.string.speed),
            getString(R.string.acceleration),
            getString(R.string.throttle),
            getString(R.string.breakEvents),
            getString(R.string.rpm),
        )

        val vals = arrayOf(
            String.format(Locale.US, "%01.01f", data.speedScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.timeScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.throttleScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.consumptionScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.rpmScoreLastTracklog.toFloat()),
        )

        val background = arrayOf(
            setBackgroundScore(data.speedScoreLastTracklog),
            setBackgroundScore(data.timeScoreLastTracklog),
            setBackgroundScore(data.throttleScoreLastTracklog),
            setBackgroundScore(data.consumptionScoreLastTracklog),
            setBackgroundScore(data.rpmScoreLastTracklog),
        )

        val icons = arrayOf(
            if(data.speedScoreLastTracklog < data.speedScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.timeScoreLastTracklog < data.timeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.throttleScoreLastTracklog < data.throttleScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.consumptionScoreLastTracklog < data.consumeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.rpmScoreLastTracklog < data.rpmScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
        )

        val average = arrayOf(
            getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.speedScoreAvg)),
            getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.timeScoreAvg)),
            getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.throttleScoreAvg)),
            getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.consumeScoreAvg)),
            getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.rpmScoreAvg)),
        )

        val visibility = arrayOf(
            true,
            true,
            true,
            true,
            true,
        )

        newArrayListScore.clear()
        for (i in title.indices){
            val items = ListItemScore(title[i], vals[i], icons[i].getDrawable(requireContext())!!, background[i], average[i], visibility[i])
            newArrayListScore.add(items)
        }
        runOnMainThread {
            recyclerViewScore.setList(newArrayListScore)
        }
        lastUpdate = getString(R.string.update, data.date)
    }

    private fun dataInitializeScoreBLE(data: DashboardData) {

        if(data.ecoDrivingScoreLastTracklog == 0.0 && data.drivingScoreLastTracklog == 0.0) {
            binding.scoredata.visibility = View.GONE
        } else {
            binding.scoredata.visibility = View.VISIBLE
        }

        newArrayListScore = arrayListOf()
        mData.clear()
        mDataAverage.clear()
        mDataBackground.clear()

        rotationDriving = if (data.drivingScoreLastTracklog > data.scoreAvg) 180 else 0
        rotationEco = if (data.ecoDrivingScoreLastTracklog > data.scoreEcoAvg) 180 else 0

        mData.add(RadarHolder("Speed", (data.speedScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Time", (data.timeScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Throttle", (data.throttleScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("Consume", (data.consumptionScoreLastTracklog*10).toInt()))
        mData.add(RadarHolder("RPM", (data.rpmScoreLastTracklog*10).toInt()))

        mDataAverage.add(RadarHolder("Speed", (data.speedScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Time", (data.timeScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Throttle", (data.throttleScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("Consume", (data.consumeScoreAvg*10).toInt()))
        mDataAverage.add(RadarHolder("RPM", (data.rpmScoreAvg*10).toInt()))

        mDataBackground.add(RadarHolder("Speed", 0))
        mDataBackground.add(RadarHolder("Time", 0))
        mDataBackground.add(RadarHolder("Throttle", 0))
        mDataBackground.add(RadarHolder("Consume", 0))
        mDataBackground.add(RadarHolder("RPM", 0))

        binding.progressEco.progress = data.ecoDrivingScoreLastTracklog.toFloat()*10
        binding.txtEco.text =  String.format(Locale.US, "%01.01f", (data.ecoDrivingScoreLastTracklog.toFloat()))
        binding.progressSafety.progress = data.drivingScoreLastTracklog.toFloat()*10
        binding.txtSafety.text = String.format(Locale.US, "%01.01f", (data.drivingScoreLastTracklog.toFloat()))

        binding.radar.setData(mDataAverage)
        binding.radar2.setData(mData)
        binding.radar3.setData(mDataBackground)

        binding.averageEco.text = getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.scoreEcoAvg))
        binding.averageDriving.text = getString(R.string.average_title, String.format(Locale.US, "%01.01f", data.scoreAvg))

        if (data.drivingScoreLastTracklog >= 8) {
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorLightGreen)
        } else if (data.drivingScoreLastTracklog >= 6 && data.drivingScoreLastTracklog < 8){
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorYellow)
        } else if (data.drivingScoreLastTracklog >= 4 && data.drivingScoreLastTracklog < 6){
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorOrange)
        } else {
            binding.progressSafety.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorRed)
        }

        if (data.ecoDrivingScoreLastTracklog >= 8) {
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorLightGreen)
        } else if (data.ecoDrivingScoreLastTracklog >= 6 && data.ecoDrivingScoreLastTracklog < 8){
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorYellow)
        } else if (data.ecoDrivingScoreLastTracklog >= 4 && data.ecoDrivingScoreLastTracklog < 6){
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorOrange)
        } else {
            binding.progressEco.progressBarColor = ContextCompat.getColor(requireContext(),R.color.colorRed)
        }

        val title = arrayOf(
            getString(R.string.speed),
            getString(R.string.acceleration),
            getString(R.string.throttle),
            getString(R.string.breakEvents),
            getString(R.string.rpm),
        )

        val vals = arrayOf(
            String.format(Locale.US, "%01.01f", data.speedScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.timeScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.throttleScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.consumptionScoreLastTracklog.toFloat()),
            String.format(Locale.US, "%01.01f", data.rpmScoreLastTracklog.toFloat()),
        )

        val background = arrayOf(
            setBackgroundScore(data.speedScoreLastTracklog),
            setBackgroundScore(data.timeScoreLastTracklog),
            setBackgroundScore(data.throttleScoreLastTracklog),
            setBackgroundScore(data.consumptionScoreLastTracklog),
            setBackgroundScore(data.rpmScoreLastTracklog),
        )

        val icons = arrayOf(
            if(data.speedScoreLastTracklog < data.speedScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.timeScoreLastTracklog < data.timeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.throttleScoreLastTracklog < data.throttleScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.consumptionScoreLastTracklog < data.consumeScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
            if(data.rpmScoreLastTracklog < data.rpmScoreAvg) R.drawable.ic_baseline_arrow_downward_24 else R.drawable.ic_round_arrow_upward_24,
        )

        val average = arrayOf(
            getString(R.string.average_title, "--"),
            getString(R.string.average_title, "--"),
            getString(R.string.average_title, "--"),
            getString(R.string.average_title, "--"),
            getString(R.string.average_title, "--"),
        )

        val visibility = arrayOf(
            false,
            false,
            false,
            false,
            false,
        )

        newArrayListScore.clear()
        for (i in title.indices){
            val items = ListItemScore(title[i], vals[i], icons[i].getDrawable(requireContext())!!, background[i], average[i], visibility[i])
            newArrayListScore.add(items)
        }
        runOnMainThread {
            recyclerViewScore.setList(newArrayListScore)
        }
        lastUpdate = getString(R.string.update, data.date)
    }

    fun setBackgroundScore(score: Double) : Drawable {
        return if (score >= 8) {
            resources.getDrawable(R.color.colorLightGreen)
        } else if (score < 8 && score >= 6) {
            resources.getDrawable(R.color.colorYellow)
        } else if (score < 6 && score >= 4) {
            resources.getDrawable(R.color.colorOrange)
        } else {
            resources.getDrawable(R.color.colorRed)
        }
    }

    private fun detectedBLE(bool: Boolean) {

        if (bool) {
            binding.arrowEco.visibility = View.GONE
            binding.arrowDriving.visibility = View.GONE
            binding.averageEco.text = "(avg --)"
            binding.averageEco.text = "--"
            binding.radar.visibility = View.GONE
        } else {
            binding.arrowEco.visibility = View.VISIBLE
            binding.arrowDriving.visibility = View.VISIBLE
            binding.radar.visibility = View.VISIBLE
        }

    }
}