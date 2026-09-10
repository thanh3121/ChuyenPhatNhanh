package com.example.btl_mobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EmployeeStatsFragment : Fragment() {

    private lateinit var rvFeedback: RecyclerView
    private var feedbackList = mutableListOf<Feedback>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_employee_stats, container, false)
        
        rvFeedback = view.findViewById(R.id.rv_feedback)
        rvFeedback.layoutManager = LinearLayoutManager(requireContext())
        
        loadStats(view)
        loadFeedback()
        
        return view
    }

    private fun loadStats(view: View) {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            
            // 1. Get Employee Start Date and Ma NV
            val empCursor = db.rawQuery("SELECT ma_nhan_vien, ngay_vao_lam FROM nhan_vien WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            var maNV = ""
            if (empCursor.moveToFirst()) {
                maNV = empCursor.getString(0)
                view.findViewById<TextView>(R.id.tv_stats_active_time).text = "Ngày bắt đầu: ${empCursor.getString(1)}"
            }
            empCursor.close()

            // 2. Total Orders
            val orderCursor = db.rawQuery("SELECT COUNT(*) FROM don_van_chuyen WHERE ma_nhan_vien_giao = ?", arrayOf(maNV))
            if (orderCursor.moveToFirst()) {
                view.findViewById<TextView>(R.id.tv_stats_total_orders).text = "Tổng đơn đã nhận: ${orderCursor.getInt(0)}"
            }
            orderCursor.close()
            
            // 3. Success/Fail Ratio
            val successCursor = db.rawQuery("SELECT COUNT(*) FROM don_van_chuyen WHERE ma_nhan_vien_giao = ? AND trang_thai = 'Thanh cong'", arrayOf(maNV))
            val failCursor = db.rawQuery("SELECT COUNT(*) FROM don_van_chuyen WHERE ma_nhan_vien_giao = ? AND trang_thai = 'That bai'", arrayOf(maNV))
            
            var successCount = 0
            var failCount = 0
            if (successCursor.moveToFirst()) successCount = successCursor.getInt(0)
            if (failCursor.moveToFirst()) failCount = failCursor.getInt(0)
            
            successCursor.close()
            failCursor.close()
            
            // For now, we'll just update the placeholder text or similar if we had a chart library. 
            // Since we use XML and direct DB, let's just log or show summary.
        }
    }

    private fun loadFeedback() {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            val empCursor = db.rawQuery("SELECT ma_nhan_vien FROM nhan_vien WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (empCursor.moveToFirst()) {
                val maNV = empCursor.getString(0)
                
                val cursor = db.rawQuery("SELECT * FROM danh_gia WHERE ma_nhan_vien = ? ORDER BY ngay_danh_gia DESC", arrayOf(maNV))
                feedbackList.clear()
                while (cursor.moveToNext()) {
                    feedbackList.add(Feedback(
                        maDon = cursor.getInt(cursor.getColumnIndexOrThrow("ma_don")),
                        soSao = cursor.getInt(cursor.getColumnIndexOrThrow("so_sao")),
                        binhLuan = cursor.getString(cursor.getColumnIndexOrThrow("binh_luan")),
                        ngayDanhGia = cursor.getString(cursor.getColumnIndexOrThrow("ngay_danh_gia"))
                    ))
                }
                cursor.close()
                rvFeedback.adapter = FeedbackAdapter(feedbackList)
            }
            empCursor.close()
        }
    }
}

data class Feedback(val maDon: Int, val soSao: Int, val binhLuan: String?, val ngayDanhGia: String)

class FeedbackAdapter(private val feedbacks: List<Feedback>) : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRating: TextView = view.findViewById(R.id.tv_fb_rating)
        val tvComment: TextView = view.findViewById(R.id.tv_fb_comment)
        val tvDate: TextView = view.findViewById(R.id.tv_fb_date)
        val tvOrderId: TextView = view.findViewById(R.id.tv_fb_order_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fb = feedbacks[position]
        holder.tvRating.text = "⭐".repeat(fb.soSao)
        holder.tvComment.text = fb.binhLuan
        holder.tvDate.text = fb.ngayDanhGia
        holder.tvOrderId.text = "Đơn: #${fb.maDon}"
    }

    override fun getItemCount() = feedbacks.size
}
