package com.otcengineering.vitesco.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.protobuf.Extension
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.ActivityFreezeFrameBinding
import com.otcengineering.vitesco.databinding.ActivityHomeBinding
import com.otcengineering.vitesco.model.DTCViewModel
import com.otcengineering.vitesco.model.ProfileViewModel
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.parseToString
import com.otcengineering.vitesco.view.components.TitleBar
import org.json.JSONObject

class FreezeFrameActivity : BaseActivity(){

    private val binding: ActivityFreezeFrameBinding by lazy { ActivityFreezeFrameBinding.inflate(layoutInflater) }
    private val viewModel: DTCViewModel by lazy { DTCViewModel(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.titlebar.setLeftButtonImage(R.drawable.ic_back)
        binding.titlebar.title = "DTC Freeze Frame"
        binding.titlebar.setListener(object: TitleBar.TitleBarListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRight1Click() {

            }

            override fun onRight2Click() {

            }
        })

        val id = intent.getLongExtra(Constants.EXTRA_DTC_ID, 0L)

        if (id == 0L) {
            finish()
            return
        }

        viewModel.getFreezeFrame(id).subscribe ({
            Log.d("fields", it.freezeFrameData.toString())

            val json = JSONObject(it.freezeFrameData)
            binding.engineTorqueMode.text = json.getInt("engineTorqueMode").toString()
            binding.boost.text = String.format("%d kPa", json.getInt("boost"))
            binding.engineSpeed.text = json.getInt("engineSpeed").toString() + " RPM"
            binding.enginePercentLoad.text = json.getInt("enginePercentLoad").toString() + " %"
            binding.engineCoolantTemperature.text = json.getInt("engineCoolantTemperature").toString() + " ºC"
            binding.vehicleSpeed.text = json.getInt("vehicleSpeed").toString() + " RPM"

//            binding.dtcId.text = it.id.toString()
            binding.dtcCode.text = it.dtcCode
            binding.DTCCodeRawData.text = it.dtcCodeRawData.toByteArray().parseToString()
            binding.freezeFrameType.text = it.freezeFrameType.toString()
            binding.freezeFrameRawData.text = it.freezeFrameRawData.toByteArray().parseToString()

        }, {
            Log.e("Fail", "Exception", it)
        })
    }

    companion object {
        fun newInstance(ctx: Context, dtcID: Long) {
            val intent = Intent(ctx, FreezeFrameActivity::class.java)
            intent.putExtra(Constants.EXTRA_DTC_ID, dtcID)
            ctx.startActivity(intent)
        }
    }

}