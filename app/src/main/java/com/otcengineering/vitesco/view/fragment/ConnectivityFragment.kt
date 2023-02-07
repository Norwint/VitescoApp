package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.otcengineering.otcble.BleSDK
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.FragmentConnectivityBinding
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.runOnMainThread
import id.ionbit.ionalert.IonAlert
import java.util.*


class ConnectivityFragment : Fragment() {

    private val binding: FragmentConnectivityBinding by lazy {
        FragmentConnectivityBinding.inflate(
            layoutInflater
        )
    }

    private var timer: Timer = Timer()
    private fun onTimerTick() {
        val logs = BluetoothService.getService(requireContext()).getLogTransfer()

        val pendingTracklogs: String
        if (logs.tracklog0 == logs.tracklogN) {
            pendingTracklogs = getString(R.string.no_pending_tracklogs)
        } else {
            var i = logs.tracklog0
            var cnt = 0
            while (i != logs.tracklogN) {
                cnt++
                i++
                if (i == 127) {
                    i = 0
                }
            }
            pendingTracklogs = getString(R.string.pending_tracklogs, cnt)
        }
        runOnMainThread { binding.tracklog.text = "$pendingTracklogs (${logs.tracklog0} of ${logs.tracklogN})" }

        val pendingStatuslogs: String

        if (logs.statuslog0 == logs.statuslogN) {
            pendingStatuslogs = getString(R.string.no_pending_statuslogs)
        } else {
            var i = logs.statuslog0
            var cnt = 0
            while (i != logs.statuslogN) {
                i++
                cnt++
                if (i == 127) {
                    i = 0
                }
            }
            pendingStatuslogs = getString(R.string.pending_statuslogs, cnt)
        }

        runOnMainThread { binding.statuslog.text = "$pendingStatuslogs (${logs.statuslog0} of ${logs.statuslogN})" }

        val pendingDiagnosislogs: String

        if (logs.diagnosislog0 == logs.diagnosislogN) {
            pendingDiagnosislogs = getString(R.string.no_pending_diagnosislogs)
        } else {
            var i = logs.diagnosislog0
            var cnt = 0
            while (i != logs.diagnosislogN) {
                i++
                cnt++
                if (i == 127) {
                    i = 0
                }
            }
            pendingDiagnosislogs = getString(R.string.pending_diagnosislogs, cnt)
        }

        runOnMainThread { binding.diagnosislog.text = "$pendingDiagnosislogs (${logs.diagnosislog0} of ${logs.diagnosislogN})" }

        val downloaded = BleSDK.getFileTransferProgress().first //bytes baixats
        val total = BleSDK.getFileTransferProgress().second //bytes total

        runOnMainThread { binding.statusProgressStatuslog.text = "Uploading ${BleSDK.getTransferType() ?: ""}" }

        if (total > 0) {
            val progress = 100 * (downloaded.toFloat()) / (total.toFloat())
            Log.d("ConnectivityFragment", String.format(Locale.US,
                "Bytes In: %d Bytes Total: %d Progress: %01.01f%%",
                downloaded,
                total,
                progress
            ))
            runOnMainThread {
                binding.progressStatuslog.progress = (progress).toInt()
                binding.processPercent.text = String.format(Locale.US, "%d%% (%dB of %dB)", progress.toInt(), downloaded, total)
            }
        } else {
            runOnMainThread {
                binding.progressStatuslog.progress = 0
                binding.processPercent.text = "---"
            }
        }

        runOnMainThread { binding.fwVersion.text = BluetoothService.getService(requireContext()).getFirmwareVersion() ?: "---" }

        runOnMainThread { binding.invalidateAll() }
        // progress = second / first fins a 0
    }

    fun tracklogSwitchChecked() {
        val prefs = Common.sharedPreferences
        prefs.putBoolean(Constants.Preferences.TRACKLOG_SHOULD_DOWNLOAD, !prefs.getBoolean(Constants.Preferences.TRACKLOG_SHOULD_DOWNLOAD))
    }

    fun statuslogSwitchChecked() {
        val prefs = Common.sharedPreferences
        prefs.putBoolean(Constants.Preferences.STATUSLOG_SHOULD_DOWNLOAD, !prefs.getBoolean(Constants.Preferences.STATUSLOG_SHOULD_DOWNLOAD))
    }

    fun diagnosislogSwitchChecked() {
        val prefs = Common.sharedPreferences
        prefs.putBoolean(Constants.Preferences.DIAGNOSISLOG_SHOULD_DOWNLOAD, !prefs.getBoolean(Constants.Preferences.DIAGNOSISLOG_SHOULD_DOWNLOAD))
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    fun clear() {
        val alert = IonAlert(requireContext())
        alert.titleText = getString(R.string.clear_ask)
        alert.confirmText = getString(R.string.yes)
        alert.cancelText = getString(R.string.no)

        alert.setConfirmClickListener {
            it.dismiss()
            BluetoothService.getService(requireContext()).bluetoothClear()
        }
        alert.show()
    }

    fun reset() {
        val alert = IonAlert(requireContext())
        alert.titleText = getString(R.string.reconnect_ask)
        alert.confirmText = getString(R.string.yes)
        alert.cancelText = getString(R.string.no)

        alert.setConfirmClickListener {
            it.dismiss()
            BluetoothService.getService(requireContext()).bluetoothDisconnect()
        }
        alert.show()
    }

    override fun onResume() {
        super.onResume()
        timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                onTimerTick()
            }
        }, 0, 1000)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this

        val prefs = Common.sharedPreferences

        binding.switchStatuslog.isChecked = !prefs.getBoolean(Constants.Preferences.STATUSLOG_SHOULD_DOWNLOAD)
        binding.switchTracklog.isChecked = !prefs.getBoolean(Constants.Preferences.TRACKLOG_SHOULD_DOWNLOAD)
        binding.switchDiagnosislog.isChecked = !prefs.getBoolean(Constants.Preferences.DIAGNOSISLOG_SHOULD_DOWNLOAD)

        val refreshTime = if (prefs.contains(Constants.Preferences.CLOUD_REFRESH_TIME)) {
            prefs.getFloat(Constants.Preferences.CLOUD_REFRESH_TIME, 2.0F)
        } else {
            prefs.putFloat(Constants.Preferences.CLOUD_REFRESH_TIME, 2.0F)
            2.0F
        }

        binding.refreshTime.text = "${refreshTime}s"

        val slider: Slider = binding.secondsRefresh
        slider.setLabelFormatter { value -> //It is just an example
            String.format(Locale.US, "%.0fs", value)
        }
        slider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                prefs.putFloat(Constants.Preferences.CLOUD_REFRESH_TIME, value)
                binding.refreshTime.text = "${value}s"
            }
        })
        slider.value = refreshTime

        return binding.root
    }

}