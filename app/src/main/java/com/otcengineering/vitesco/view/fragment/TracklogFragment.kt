package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.Tracklog
import com.otcengineering.vitesco.databinding.FragmentTracklogBinding
import com.otcengineering.vitesco.model.TracklogViewModel
import com.otcengineering.vitesco.utils.interfaces.OnClickListener
import com.otcengineering.vitesco.utils.showAlertDialog
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.otcengineering.vitesco.view.components.addSource
import com.ultramegasoft.radarchart.RadarHolder
import com.ultramegasoft.radarchart.RadarView
import java.util.ArrayList


class TracklogFragment : Fragment() {
    private val binding : FragmentTracklogBinding by lazy {
        FragmentTracklogBinding.inflate(layoutInflater)
    }

    var page = 1
    private val viewModel = TracklogViewModel()

    private var mData = ArrayList<RadarHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this
        binding.viewModel = viewModel


        with(binding.recyclerView) {
            addSource(
                R.layout.row_route,
                viewModel.routes, object : OnClickListener<Tracklog> {
                    override fun onItemClick(view: View, t: Tracklog) {
                        when (view.id) {
                            R.id.imageView3 -> {
                                binding.viewModel?.showRouteOptions(true)
                            }
                            else -> return
                        }
                    }
                }
            )
            setRecyclerListener {
                it as RecyclerViewAdapter<*>.ViewHolder<*>
                it.onClear()
            }
        }
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {
                    page++
                    viewModel.loadRoutes(page,requireContext())
                }
            }
        })

        viewModel.loadRoutes(page,requireContext())

        return binding.root
    }

    fun deleteRoute() {
        requireContext().showAlertDialog("DELETE", "Are you sure you want to delete the tracklog?") {
            viewModel.deleteSelectedRoutes()
        }
    }

    fun selectAllRoutes() {
        for (route in viewModel.routes) {
            route.setSelection(true)
        }
    }
}