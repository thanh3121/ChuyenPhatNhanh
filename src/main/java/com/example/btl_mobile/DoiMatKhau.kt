package com.example.btl_mobile

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DoiMatKhau : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val etOld = findViewById<EditText>(R.id.et_old_password)
        val etNew = findViewById<EditText>(R.id.et_new_password)
        val etConfirm = findViewById<EditText>(R.id.et_confirm_new_password)
        val btnChange = findViewById<Button>(R.id.btn_do_change_password)

        val dbHelper = DbHelper(this)
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        btnChange.setOnClickListener {
            val oldPass = etOld.text.toString().trim()
            val newPass = etNew.text.toString().trim()
            val confirmPass = etConfirm.text.toString().trim()

            if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6 || newPass.length > 12) {
                Toast.makeText(this, "Mật khẩu mới từ 6-12 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(this, "Xác nhận mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val cursor = db.rawQuery("SELECT mat_khau FROM tai_khoan WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            
            if (cursor.moveToFirst()) {
                val currentPass = cursor.getString(0)
                if (currentPass != oldPass) {
                    Toast.makeText(this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show()
                } else {
                    val values = android.content.ContentValues().apply {
                        put("mat_khau", newPass)
                    }
                    db.update("tai_khoan", values, "ma_tai_khoan = ?", arrayOf(userId.toString()))
                    Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            cursor.close()
        }
    }
}