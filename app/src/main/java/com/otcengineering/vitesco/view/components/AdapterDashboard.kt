package com.otcengineering.vitesco.view.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.otcengineering.vitesco.R
import com.otcengineering.vitesco.data.ListItem

class AdapterDashboard(private val newList : ArrayList<ListItem>) : RecyclerView.Adapter<AdapterDashboard.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_dashboard, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newList[position]
        holder.titleName.text = currentItem.title
        holder.valorName.text = currentItem.valor
    }

    override fun getItemCount(): Int {
        return newList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleName : TextView = itemView.findViewById(R.id.idDato)
        val valorName : TextView = itemView.findViewById(R.id.idValor)
    }

}