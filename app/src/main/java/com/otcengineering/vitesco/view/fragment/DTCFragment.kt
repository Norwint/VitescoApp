package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.databinding.FragmentDtcHistoryBinding
import com.otcengineering.vitesco.data.DTC
import com.otcengineering.vitesco.data.ListFilterDtc
import com.otcengineering.vitesco.data.ListItemScore
import com.otcengineering.vitesco.dtc.FilterMenu
import com.otcengineering.vitesco.model.DTCViewModel
import com.otcengineering.vitesco.service.BluetoothService
import com.otcengineering.vitesco.utils.*
import com.otcengineering.vitesco.utils.interfaces.OnClickListener
import com.otcengineering.vitesco.view.activity.FreezeFrameActivity
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.otcengineering.vitesco.view.components.addSource
import io.reactivex.rxjava3.core.Observable
import java.util.ArrayList

class DTCFragment : Fragment() {
    private val binding : FragmentDtcHistoryBinding by lazy {
        FragmentDtcHistoryBinding.inflate(layoutInflater)
    }

    private var filter = "all"
    private var filterCode = "-1"
    private lateinit var newArrayListScore: ArrayList<ListFilterDtc>
    private var i = 1L

    private val viewModel : DTCViewModel by lazy {
        DTCViewModel(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this
        binding.viewModel = viewModel

        getDTC()

        selectedFilter("all", "-1")

        with(binding.recyclerView) {
            addSource(
                R.layout.row_dtc,
                viewModel.routes, object : OnClickListener<DTC> {
                    override fun onItemClick(view: View, t: DTC) {
                        if (view.id == R.id.showFreezeData) {
                            FreezeFrameActivity.newInstance(requireContext(),t.id)
                        }
                    }
                }
            )
            setRecyclerListener {
                it as RecyclerViewAdapter<*>.ViewHolder<*>
                it.onClear()
            }
        }

        return binding.root
    }

    private fun getPage(page: Int) {
        viewModel.getDTCHistory(Common.sharedPreferences.getLong(Constants.Preferences.VEHICLE_ID, 0L), filter, filterCode, page).subscribe {
            getPage(page + 1)
        }
    }

    fun getDTC() {
        viewModel.clear()
        getPage(1)
    }

    fun filter() {
        requireContext().showAlertDialogFilter(layoutInflater) { selected, filter ->
            this.filter = selected
            this.filterCode = filter
            getDTC()
            selectedFilter(selected,filter)
        }

    }

    fun selectedFilter(filter: String, filterCode: String) {
        val recyclerViewScore : RecyclerViewAdapter<ListFilterDtc> = RecyclerViewAdapter(R.layout.row_filter)
        binding.filterRv.adapter = recyclerViewScore

        newArrayListScore = arrayListOf()
        newArrayListScore.clear()

        var filtersCode: ListFilterDtc? = null

        if(filterCode != "-1") {
            filtersCode = ListFilterDtc(filterCode)
        }

        val filters: ListFilterDtc = when (filter) {
            "activated" -> {
                ListFilterDtc("Enabled")
            }
            "deactivated" -> {
                ListFilterDtc("Disabled")
            }
            else -> {
                ListFilterDtc("All")
            }
        }

        newArrayListScore.add(filters)
        if (filtersCode != null) {
            newArrayListScore.add(filtersCode)
        }
        recyclerViewScore.setList(newArrayListScore)
    }

    fun clearDtc() {
        requireContext().showAlertDialog("CLEAR", "Are you sure you want to clear DTCs?") {
            BluetoothService.getService(requireContext()).clearDTC()
        }

    }

    fun showDummy() {
        requireContext().showAlertDialogOk("") {}
    }
}