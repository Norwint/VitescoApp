package com.otcengineering.vitesco.view.activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.otc.alice.api.model.Community.UserCommunity
import com.otc.alice.api.model.General
import com.otc.alice.api.model.MyDrive
import com.otcengineering.otcble.BleSDK
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.DashboardData
import com.otcengineering.vitesco.data.FiltersData
import com.otcengineering.vitesco.data.Ranking
import com.otcengineering.vitesco.data.Tracklog
import com.otcengineering.vitesco.databinding.ActivityRankingBinding
import com.otcengineering.vitesco.model.ProfileViewModel
import com.otcengineering.vitesco.model.RankingViewModel
import com.otcengineering.vitesco.model.TracklogViewModel
import com.otcengineering.vitesco.utils.interfaces.OnClickListener
import com.otcengineering.vitesco.utils.runOnMainThread
import com.otcengineering.vitesco.view.components.RecyclerViewAdapter
import com.otcengineering.vitesco.view.components.TitleBar
import com.otcengineering.vitesco.view.components.addSource

class RankingActivity : BaseActivity() {
    private val binding: ActivityRankingBinding by lazy { ActivityRankingBinding.inflate(layoutInflater) }

    private val myPosition = -1

    var page = 1

    private var type = MyDrive.DriveType.ECO_DRIVING_SCORE

    private var time = General.TimeType.DAILY

    private val viewModel = RankingViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rankingTitleBar.setListener(object: TitleBar.TitleBarListener {
            override fun onLeftClick() {
                finish()
            }

            override fun onRight1Click() {

            }

            override fun onRight2Click() {

            }
        })

        binding.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    if (tab.position == 0) {
                        page = 1
                        time = General.TimeType.DAILY
                        viewModel.loadRankings(page, type, time, this@RankingActivity)
                    } else if (tab.position == 1) {
                        page = 1
                        time = General.TimeType.WEEKLY
                        viewModel.loadRankings(page, type, time, this@RankingActivity)
                    } else {
                        page = 1
                        time = General.TimeType.MONTHLY
                        viewModel.loadRankings(page, type, time, this@RankingActivity)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.compatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    page = 1
                    type = MyDrive.DriveType.ECO_DRIVING_SCORE
                    viewModel.loadRankings(page, type, time, this@RankingActivity)
                } else {
                    page = 1
                    type = MyDrive.DriveType.DRIVING_SCORE_1
                    viewModel.loadRankings(page, type, time, this@RankingActivity)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        with(binding.rankingRecycler) {
            addSource(
                R.layout.row_ranking,
                viewModel.rankings, object : OnClickListener<Ranking> {
                    override fun onItemClick(view: View, t: Ranking) {
                        when (view.id) {
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

        binding.rankingRecycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1)) {
                    page++
                    viewModel.loadRankings(page, type, time, this@RankingActivity)
                }
            }
        })

        viewModel.loadRankings(page, type, time, this@RankingActivity)

    }

    private fun goToMyPosition() {
        if (myPosition != -1) {
            moveListToMyPosition()
        } else {
            getMyPosition()
        }
    }

    private fun moveListToMyPosition() {
        if (myPosition != -1) {
            binding.rankingRecycler.smoothScrollToPosition(myPosition)
        }
    }

    private fun getMyPosition() {
//        if (ConnectionUtils.isOnline(applicationContext)) {
//            val endpoint: String = Endpoints.RANKING_POSITION
//            val getRankingInfoTask: com.otcengineering.white_app.activities.RankingActivity.GetRankingInfoTask =
//                com.otcengineering.white_app.activities.RankingActivity.GetRankingInfoTask()
//            getRankingInfoTask.execute(endpoint)
//        } else {
//            ConnectionUtils.showOfflineToast()
//        }
    }

}
