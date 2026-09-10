package com.example.btl_mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class HoSoCaNhan : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val etName = view.findViewById<EditText>(R.id.et_profile_name)
        val etPhone = view.findViewById<EditText>(R.id.et_profile_phone)
        val btnUpdatePhone = view.findViewById<Button>(R.id.btn_update_phone)
        val btnAddress = view.findViewById<Button>(R.id.btn_address_management)
        val btnChangePass = view.findViewById<Button>(R.id.btn_change_password)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT ho_ten, so_dien_thoai FROM khach_hang WHERE ma_tai_khoan = ?",
                arrayOf(userId.toString())
            )
            if (cursor.moveToFirst()) {
                etName.setText(cursor.getString(0))
                etPhone.setText(cursor.getString(1))
            }
            cursor.close()
        }

        btnUpdatePhone.setOnClickListener {
            val newPhone = etPhone.text.toString().trim()
            if (!newPhone.startsWith("0") || newPhone.length != 10 || !newPhone.all { it.isDigit() }) {
                Toast.makeText(requireContext(), "Số điện thoại phải đủ 10 số và bắt đầu bằng 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val values = android.content.ContentValues().apply {
                put("so_dien_thoai", newPhone)
            }
            val rows = db.update("khach_hang", values, "ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (rows > 0) {
                Toast.makeText(requireContext(), "Cập nhật số điện thoại thành công", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        }

        btnAddress.setOnClickListener {
            startActivity(Intent(requireContext(), QuanLyDiaChi::class.java))
        }

        btnChangePass.setOnClickListener {
            startActivity(Intent(requireContext(), DoiMatKhau::class.java))
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