package com.otcengineering.vitesco.utils.interfaces

import android.view.View

interface OnClickListener<T> {
    fun onItemClick(view: View, t: T)
}