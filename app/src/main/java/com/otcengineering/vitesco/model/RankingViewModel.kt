package com.otcengineering.vitesco.model

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.otc.alice.api.model.*
import com.otc.alice.api.model.General.TimeType
import com.otc.alice.api.model.MyDrive.DriveType
import com.otc.alice.api.model.MyDrive.RankingResponse
import com.otcengineering.vitesco.data.*
import com.otcengineering.vitesco.utils.Common
import com.otcengineering.vitesco.utils.Constants
import com.otcengineering.vitesco.utils.executeAPI
import com.otcengineering.vitesco.utils.runOnMainThread
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

class RankingViewModel {

    var rankings = ObservableArrayList<Ranking>()
    val routeLoading = ObservableBoolean()

    private val profileViewModel = ProfileViewModel()

    fun loadRankings(page: Int, typeRanking: DriveType, time: TimeType, ctx: Context) {
        if(page == 1) {
            rankings.clear()
        }
        getRanking(page, typeRanking, time, ctx)
    }

    fun getRanking(page: Int, typeRanking: DriveType, time: TimeType, ctx: Context) {
        executeAPI(null) {

            val build = MyDrive.Ranking.newBuilder()
                .setPage(page)
                .setDriveType(typeRanking)
                .setTypeTime(time)
                .build()

            val response = Common.network.driveApi.driveRanking(build).execute().body()
            if (response == null || response.status != Shared.OTCStatus.SUCCESS) {
                return@executeAPI
            } else {
                val rsp = response.data.unpack(RankingResponse::class.java)

                for (ranking in rsp.usersList) {
                    if (rankings.firstOrNull { it.id == ranking.id } != null) {
                        continue
                    }
                    val rank = Ranking(
                        ctx,
                        ranking.id,
                        ranking.name,
                        ranking.position,
                        ranking.score,
                        ranking.imageId
                    )
                    getImage(ranking.imageId, rank, ctx)
                    runOnMainThread { this.rankings.add(rank) }
                }
            }
            runOnMainThread { routeLoading.set(false) }
        }
    }

    fun getImage(id: Long, ranking: Ranking, ctx: Context) {
        if (id != 0L) {
            profileViewModel.getImage(id, ctx).subscribe({
                runOnMainThread {
                    ranking.drawable = it
                    ranking.reload()
                }
            }, {})
        }
    }


}