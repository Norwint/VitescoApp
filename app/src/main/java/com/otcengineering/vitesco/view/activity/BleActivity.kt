package com.otcengineering.vitesco.view.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.otcengineering.otcble.*
import com.otcengineering.otcble.interfaces.OnWrite
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.ActivityBleBinding
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.service.LocationService
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.CustomTimer
import com.otcengineering.vitesco.utils.runOnMainThread

class BleActivity : BaseActivity() {
    private val binding: ActivityBleBinding by lazy {
        ActivityBleBinding.inflate(layoutInflater)
    }

    private var timer: CustomTimer? = null
    private var bleTimer: CustomTimer? = null

    private var readingFile = false
    private var restartTime = 0

    private val connectedColor: Int by lazy {
        ContextCompat.getColor(this, R.color.colorBlue)
    }

    private val disconnectedColor: Int by lazy {
        ContextCompat.getColor(this, R.color.colorRed)
    }

    companion object {
        fun newInstance(ctx: Context) = ctx.startActivity(Intent(ctx, BleActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        LocationService.getService(this).getLocation()
        BluetoothService.getService(this).executeService()

        bleTimer = CustomTimer()
        bleTimer?.createAndRun(0, 250) {
            runOnMainThread {
                binding.bleImage.imageTintList = ColorStateList.valueOf(
                    if (BleSDK.isConnected())
                        connectedColor else disconnectedColor
                )
                binding.heartbeat.text = BluetoothService.getService(this).lastHeartbeat

                val status = BleSDK.getStatus()
                var txt = ""

                for (st in status.getEntries()) {
                    txt += st.key
                    txt += ": "
                    txt += when (st.value.getType()) {
                        Status.DataEntry.Type.Integer -> st.value.getInt().toString()
                        Status.DataEntry.Type.Boolean -> st.value.getBoolean().toString()
                        Status.DataEntry.Type.Float -> st.value.getFloat().toString()
                        Status.DataEntry.Type.Data -> Utils.getInstance().byteToString(st.value.getData())
                    }
                    txt += "\n"
                }
                binding.text.text = txt
            }
        }
    }
}