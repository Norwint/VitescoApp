package com.otcengineering.vitesco.view.activity

import android.content.Context
import android.content.Intent
import android.net.Network
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.otc.alice.api.model.Shared
import com.otc.alice.api.model.Welcome
import com.otcengineering.otcble.NetworkSDK
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.ActivityLoginBinding
import com.otcengineering.vitesco.databinding.ActivitySplashBinding
import com.otcengineering.vitesco.model.OtcException
import com.otcengineering.vitesco.model.WelcomeViewModel
import com.otcengineering.vitesco.utils.*
import com.otcengineering.vitesco.utils.Constants.BASE_URL

class LoginActivity : BaseActivity() {
    private val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel: WelcomeViewModel by lazy { WelcomeViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            checkLogin(binding.etUsername.text.toString(), binding.etPassword.text.toString())
            //checkLogin("0034647295331", "Patata.123")
        }

    }

    private fun checkLogin(phone: String, password: String) {
        if (!isLoadingShown()) showLoading()

        viewModel.login(phone, password).subscribe({
            viewModel.getProfile().subscribe({ profile ->
                // Common.serialNumber = profile.userProfilePart2.tcuSerialNumber
                // Common.sharedPreferences.putString(Constants.Preferences.SERIAL_NUMBER, profile.userProfilePart2.tcuSerialNumber)
                dismissLoading()
                getVehicleList()
                goToHome()
            }, { error ->
                if (error is OtcException) {
                    dismissLoading()
                    Popups.showErrorPopup(this, "Login Error:", error.status.name)
                }
            })
        }, { error ->
            if (error is OtcException) {
                when (error.status) {
                    Shared.OTCStatus.NEW_MOBILE -> changePhone(phone, password)
                    else -> {
                        dismissLoading()
                        Popups.showErrorPopup(this, "Login Error:", error.status.name)
                    }
                }
            }
        })
    }

    private fun getVehicleList() {
        viewModel.getVehicleList().subscribe({ list ->
            val first = list.first()
            Common.sharedPreferences.putLong(Constants.Preferences.VEHICLE_ID, first.id)
        }, {})
    }

    private fun changePhone(phone: String, password: String) {
        viewModel.changePhone(phone, password).subscribe({
            checkLogin(phone, password)
        }, { error ->
            dismissLoading()
            if (error is OtcException) {
                Popups.showErrorPopup(this, "Login Error:", error.status.name)
            }
        })
    }

    private fun goToHome() = HomeActivity.newInstance(this)

    companion object {
        fun newInstance(ctx: Context) = ctx.startActivity(LoginActivity::class.java)
    }
}