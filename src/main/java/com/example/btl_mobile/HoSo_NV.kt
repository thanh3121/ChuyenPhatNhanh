package com.example.btl_mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class HoSo_NV : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_employee_profile, container, false)
        
        val tvId = view.findViewById<TextView>(R.id.tv_emp_id)
        val tvName = view.findViewById<TextView>(R.id.tv_emp_name)
        val tvHometown = view.findViewById<TextView>(R.id.tv_emp_hometown)
        val tvPhone = view.findViewById<TextView>(R.id.tv_emp_phone)
        val tvStatus = view.findViewById<TextView>(R.id.tv_emp_status)
        val tvUser = view.findViewById<TextView>(R.id.tv_emp_username)
        val tvPass = view.findViewById<TextView>(R.id.tv_emp_password)
        val btnLogout = view.findViewById<Button>(R.id.btn_emp_logout)

        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("""
                SELECT nv.ma_nhan_vien, nv.ho_ten, nv.que_quan, nv.so_dien_thoai, nv.trang_thai, tk.tai_khoan, tk.mat_khau 
                FROM nhan_vien nv
                JOIN tai_khoan tk ON nv.ma_tai_khoan = tk.ma_tai_khoan
                WHERE tk.ma_tai_khoan = ?
            """, arrayOf(userId.toString()))

            if (cursor.moveToFirst()) {
                tvId.text = "Mã NV: ${cursor.getString(0)}"
                tvName.text = "Họ tên: ${cursor.getString(1)}"
                tvHometown.text = "Quê quán: ${cursor.getString(2)}"
                tvPhone.text = "Số điện thoại: ${cursor.getString(3)}"
                tvStatus.text = "Trạng thái: ${cursor.getString(4)}"
                tvUser.text = "Tài khoản: ${cursor.getString(5)}"
                tvPass.text = "Mật khẩu: ${cursor.getString(6)}"
            }
            cursor.close()
        }

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), DangNhapActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
