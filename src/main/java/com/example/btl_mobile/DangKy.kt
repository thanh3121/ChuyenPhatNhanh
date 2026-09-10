package com.example.btl_mobile

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DangKy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullname = findViewById<EditText>(R.id.et_reg_fullname)
        val etPhone = findViewById<EditText>(R.id.et_reg_phone)
        val etUser = findViewById<EditText>(R.id.et_reg_username)
        val etPass = findViewById<EditText>(R.id.et_reg_password)
        val etConfirmPass = findViewById<EditText>(R.id.et_reg_confirm_password)
        val btnDoReg = findViewById<Button>(R.id.btn_do_register)

        val dbHelper = DbHelper(this)

        btnDoReg.setOnClickListener {
            val name = etFullname.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()

            if (name.isBlank() || phone.isBlank() || user.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation logic
            if (!phone.startsWith("0") || phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Số điện thoại phải đủ 10 số và bắt đầu bằng 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user.length < 6) {
                Toast.makeText(this, "Tài khoản tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6 || pass.length > 12) {
                Toast.makeText(this, "Mật khẩu từ 6-12 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase

            // Unique checks
            val userCursor = db.rawQuery("SELECT 1 FROM tai_khoan WHERE tai_khoan = ?", arrayOf(user))
            if (userCursor.moveToFirst()) {
                Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show()
                userCursor.close()
                return@setOnClickListener
            }
            userCursor.close()

            val phoneCursor = db.rawQuery("SELECT 1 FROM khach_hang WHERE so_dien_thoai = ?", arrayOf(phone))
            if (phoneCursor.moveToFirst()) {
                Toast.makeText(this, "Số điện thoại đã được đăng ký", Toast.LENGTH_SHORT).show()
                phoneCursor.close()
                return@setOnClickListener
            }
            phoneCursor.close()

            db.beginTransaction()
            try {
                // 1. Insert Tai Khoan
                val accValues = ContentValues().apply {
                    put("tai_khoan", user)
                    put("mat_khau", pass)
                    put("loai_tai_khoan", "khach_hang")
                    put("trang_thai", "Hoat dong")
                }
                val rowId = db.insert("tai_khoan", null, accValues)
                
                if (rowId != -1L) {
                    // 2. Insert Khach Hang
                    val cusValues = ContentValues().apply {
                        put("ma_tai_khoan", rowId)
                        put("ho_ten", name)
                        put("so_dien_thoai", phone)
                    }
                    db.insert("khach_hang", null, cusValues)
                    db.setTransactionSuccessful()
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Lỗi khi tạo tài khoản", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi: " + e.message, Toast.LENGTH_SHORT).show()
            } finally {
                db.endTransaction()
            }
        }
    }
}