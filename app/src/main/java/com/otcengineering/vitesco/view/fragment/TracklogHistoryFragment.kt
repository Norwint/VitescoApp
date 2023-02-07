package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.FragmentTracklogHistoryBinding

class TracklogHistoryFragment : Fragment() {

    private val binding: FragmentTracklogHistoryBinding by lazy { FragmentTracklogHistoryBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracklog_history, container, false)
    }

}
