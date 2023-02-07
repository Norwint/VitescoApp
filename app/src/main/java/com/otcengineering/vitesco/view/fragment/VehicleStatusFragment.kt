package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.FragmentVehicleStatusBinding

class VehicleStatusFragment : Fragment() {
    private val binding: FragmentVehicleStatusBinding by lazy { FragmentVehicleStatusBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vehicle_status, container, false)
    }

}