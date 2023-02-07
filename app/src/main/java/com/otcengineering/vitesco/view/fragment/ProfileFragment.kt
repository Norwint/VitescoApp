package com.otcengineering.vitesco.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.BasicItem
import com.otcengineering.vitesco.databinding.FragmentProfileBinding
import com.otcengineering.vitesco.model.ProfileViewModel
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.TimeUtils
import com.otcengineering.vitesco.utils.interfaces.OnClickListener
import com.otcengineering.vitesco.utils.runOnMainThread
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.otcengineering.vitesco.view.components.addSource

class ProfileFragment : Fragment() {
    private val binding: FragmentProfileBinding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }

    private val viewModel: ProfileViewModel by lazy {
        ProfileViewModel()
    }

    private val items = ObservableArrayList<BasicItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(binding.recyclerView) {
            addSource(
                R.layout.row_basic,
                items, object : OnClickListener<BasicItem> {
                    override fun onItemClick(view: View, t: BasicItem) {

                    }
                }
            )
            setRecyclerListener {
                it as RecyclerViewAdapter<*>.ViewHolder<*>
                it.onClear()
            }
        }

        viewModel.getUserProfile().subscribe({ profile ->
            runOnMainThread {
                items.add(BasicItem("Username", profile.userProfilePart1.username))
                items.add(BasicItem("Email", profile.userProfilePart1.email))
                items.add(BasicItem("VIN", Common.sharedPreferences.getString(Constants.Preferences.VIN)))
                items.add(BasicItem("Phone Number", profile.userProfilePart1.phone))
                items.add(BasicItem("Serial Number", Common.serialNumber))
                items.add(BasicItem("Name", profile.userProfilePart2.name))
                items.add(BasicItem("Sex", profile.userProfilePart2.sexType.name.capitalize()))
                items.add(BasicItem("Blood Type", profile.userProfilePart2.bloodType.name.capitalize()))
                items.add(BasicItem("Birthdate", TimeUtils.serverTimeParse(profile.userProfilePart2.birthdayDate, "dd/MM/yyyy")))
                items.add(BasicItem("Country", ""))
                items.add(BasicItem("State", profile.userProfilePart2.state))
                items.add(BasicItem("City", profile.userProfilePart2.city))
                items.add(BasicItem("Location", profile.userProfilePart2.homeLocation))
                items.add(BasicItem("Address", profile.userProfilePart2.address))

                viewModel.getCountry(profile.userProfilePart2.countryId).subscribe({ name ->
                    runOnMainThread {
                        items[8] = BasicItem("Country:", name)

                        binding.invalidateAll()
                    }
                }, {
                    Log.e("ProfileFragment", "Exception", it)
                })
            }
        }, {
            Log.e("ProfileFragment", "Exception", it)
        })

        return binding.root
    }
}