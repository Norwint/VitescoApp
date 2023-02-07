package com.otcengineering.vitesco.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.otcengineering.otcble.MySharedPreferences
import com.otcengineering.otcble.NetworkSDK
import com.otcengineering.vitesco.BuildConfig
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.ActivitySplashBinding
import com.otcengineering.vitesco.model.OtcException
import com.otcengineering.vitesco.model.WelcomeViewModel
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.utils.*
import com.otcengineering.vitesco.utils.Constants.BASE_URL
import pub.devrel.easypermissions.EasyPermissions

class SplashActivity : BaseActivity() {

    private val binding: ActivitySplashBinding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private val viewModel: WelcomeViewModel by lazy { WelcomeViewModel() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Common.sharedPreferences.putString(Constants.Preferences.FIREBASE_TOKEN, token)
        }

        if (BuildConfig.BLE) {
            if (BluetoothService.checkPermissions(this)) {
                BleActivity.newInstance(this)
            }
        } else {
            if (Common.network.tokenSession.getAuthToken().isNotEmpty()) {
                runDelayed(250) {
                    showLoading()
                    refreshLogin()
                }
            } else {
                runDelayed(3000) {
                    LoginActivity.newInstance(this)
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Exception", e)
        }
        BleActivity.newInstance(this)
    }

    private fun refreshLogin() {
        val phoneNumber = Common.sharedPreferences.getString(Constants.Preferences.PHONE_NUMBER)
        val password = Common.sharedPreferences.getString(Constants.Preferences.PASSWORD)
        viewModel.login(phoneNumber, password).subscribe({
            dismissLoading()
            Common.serialNumber = Common.sharedPreferences.getString(Constants.Preferences.SERIAL_NUMBER)
            HomeActivity.newInstance(this)
            finish()
        }, {
            dismissLoading()
            if (it is OtcException) {
                LoginActivity.newInstance(this)
            } else {
                HomeActivity.newInstance(this)
            }
            finish()
        })
    }

}