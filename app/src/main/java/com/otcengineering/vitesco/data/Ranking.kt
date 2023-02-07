package com.otcengineering.vitesco.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.BaseObservable
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.utils.getDrawable
import java.util.*

class Ranking (
    var ctx: Context,
    var id: Long,
    var name: String,
    var position: Long,
    var score: Float,
    var image: Long
    ) : BaseObservable() {

    private lateinit var newArrayListScore: ArrayList<Ranking>
    var drawable : Drawable? = null

    fun getTitle(): String = name

    fun getCup() : Boolean {
        return false
    }

    fun getVisibility() : Int {

        return if (position.toInt() == 1) {
            View.VISIBLE
        } else if(position in 2..5) {
            View.VISIBLE
        } else {
            View.GONE
        }

    }

    fun getDrawableMedal(): Drawable {
        return if(position.toInt() == 1) {
            ctx.getDrawable(R.drawable.dashboard_icons3)!!
        } else if(position.toInt() in 2..5) {
            ctx.getDrawable(R.drawable.medal)!!
        } else {
            ctx.getDrawable(R.drawable.medal)!!
        }
    }

    fun getPositions(): String {
        return ctx.getString(R.string.position, position.toString())
    }
    fun getScores(): String {
        return ctx.getString(R.string.score, String.format(Locale.US, "%01.01f", score))
    }
    fun getDrawables(): Drawable {
        return if(drawable == null) {
            ctx.getDrawable(R.drawable.user_placeholder_correct)!!
        } else {
            drawable!!
        }
    }

    fun reload() {
        notifyChange()
    }

}