package com.otcengineering.vitesco.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.PushNotification
import com.otcengineering.vitesco.databinding.FragmentNotificationHistoryBinding
import com.otcengineering.vitesco.model.PushNotificationViewModel
import com.otcengineering.vitesco.utils.interfaces.OnClickListener
import com.otcengineering.vitesco.utils.showAlertDialog
import com.otcengineering.vitesco.view.components.addSource

class NotificationHistoryFragment : Fragment() {

    private val binding: FragmentNotificationHistoryBinding by lazy { FragmentNotificationHistoryBinding.inflate(layoutInflater) }
    private val viewModel : PushNotificationViewModel by lazy {
        PushNotificationViewModel()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.fragment = this
        binding.viewModel = viewModel

        viewModel.getNotificationList(requireContext())

        with(binding.recyclerView) {
            addSource(
                R.layout.row_push_notification,
                viewModel.routes,
                object : OnClickListener<PushNotification> {
                    override fun onItemClick(view: View, t: PushNotification) {
                        t.toggleSelected()
                        binding.invalidateAll()
                    }
                }
            )
        }

        binding.button6.setOnClickListener {
            deleteRoute(requireContext())
        }

        binding.button4.setOnClickListener {
            deleteRoute(requireContext())
        }

        return binding.root
    }

    fun deleteRoute(ctx: Context) {
        requireContext().showAlertDialog("DELETE", "Are you sure you want to delete the notifications?") {
            viewModel.deleteSelectedRoutes(ctx)
        }
    }

    fun selectAllRoutes() {
        for (route in viewModel.routes) {
            route.setSelection(true)
        }
    }

    // Low Oil pressure ON/OFF
}