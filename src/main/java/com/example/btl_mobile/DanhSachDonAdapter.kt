package com.example.btl_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DanhSachDonAdapter(private val orders: List<DonVanChuyen>, private val onItemClick: (DonVanChuyen) -> Unit) : RecyclerView.Adapter<DanhSachDonAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tv_order_id)
        val tvStatus: TextView = view.findViewById(R.id.tv_order_status)
        val tvAddress: TextView = view.findViewById(R.id.tv_order_address)
        val tvDate: TextView = view.findViewById(R.id.tv_order_date)
        val tvPayment: TextView = view.findViewById(R.id.tv_order_payment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.tvId.text = "Mã đơn: #${order.maDon}"
        holder.tvStatus.text = order.trangThai
        holder.tvAddress.text = "Giao: ${order.diaChiGiao}"
        holder.tvDate.text = "Ngày tạo: ${order.ngayTao}"
        holder.tvPayment.text = "Tổng thanh toán: ${String.format(java.util.Locale.getDefault(), "%,.0f", order.tongThanhToan)}đ"
        
        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount() = orders.size
}