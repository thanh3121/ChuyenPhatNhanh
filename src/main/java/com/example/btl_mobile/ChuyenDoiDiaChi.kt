package com.example.btl_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChuyenDoiDiaChi(private val addresses: List<Address>) : RecyclerView.Adapter<ChuyenDoiDiaChi.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFull: TextView = view.findViewById(R.id.tv_address_full)
        val tvRegion: TextView = view.findViewById(R.id.tv_address_region)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val addr = addresses[position]
        holder.tvFull.text = addr.diaChiChiTiet
        holder.tvRegion.text = "Miền: ${addr.mien} - Tỉnh: ${addr.tinhThanh} - Xã/Phường: ${addr.xaPhuong}"
    }

    override fun getItemCount() = addresses.size
}