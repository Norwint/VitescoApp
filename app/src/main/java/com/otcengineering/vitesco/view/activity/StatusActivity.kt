package com.otcengineering.vitesco.view.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.otcengineering.otcble.BleSDK
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.DashboardData
import com.otcengineering.vitesco.data.FiltersData
import com.otcengineering.vitesco.databinding.ActivityRankingBinding
import com.otcengineering.vitesco.databinding.ActivityStatusBinding
import com.otcengineering.vitesco.model.DashboardViewModel
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.utils.runOnMainThread
import com.otcengineering.vitesco.view.components.TitleBar

class StatusActivity : BaseActivity() {
    private val binding: ActivityStatusBinding by lazy { ActivityStatusBinding.inflate(layoutInflater) }

    private val viewModel: DashboardViewModel by lazy { DashboardViewModel() }

    private var coordinatesString = "---"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rankingTitleBar.setListener(object: TitleBar.TitleBarListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRight1Click() {

            }

            override fun onRight2Click() {

            }
        })

        viewModel.getDashboardData().subscribe({ dd ->
            coordinatesString = "${dd.dashboardData.latitude}, ${dd.dashboardData.longitude}"
            runOnMainThread {
                dataFilterInitialize(dd.filtersData)

                binding.totalOdometer.text = dd.dashboardData.distanceAccumulation.toString() + " Km"
                binding.dateBottom.text = dd.dashboardData.date
            }
        }, {
            dataFilterInitialize(FiltersData())
        })

    }

    private fun dataFilterInitialize(data: FiltersData) {

//        binding.mil.text = booleanString(data.mil)
//        binding.warningLamp.text = booleanString(data.warningLamp)
//        binding.bankLamp.text = booleanString(data.bankLamp)
//        binding.ecuApsLevel.text = booleanString(data.apsLevel)
//        binding.rowFuelLowLevel.text = booleanString(data.fuelLowLevel)

        if(data.mil) {
            binding.milStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRed))
        } else {
            binding.milStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen))
        }

        if(data.warningLamp) {
            binding.warningStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRed))
        } else {
            binding.warningStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen))
        }

        if(data.bankLamp) {
            binding.bankStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRed))
        } else {
            binding.bankStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen))
        }

        if(data.apsLevel) {
            binding.apsStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRed))
        } else {
            binding.apsStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen))
        }

        if(data.fuelLowLevel) {
            binding.fuelStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorRed))
        } else {
            binding.fuelStatus.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen))
        }

        if (data.mil || data.warningLamp || data.bankLamp || data.apsLevel || data.fuelLowLevel) {
            binding.statusWorking.text = getString(R.string.warning_detected)
            binding.warningIcon.visibility = View.VISIBLE
        }
    }

    private fun booleanString(bool: Boolean) : String {
       return if(bool) "On" else "Off"
    }

}