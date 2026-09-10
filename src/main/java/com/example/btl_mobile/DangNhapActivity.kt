package com.example.btl_mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.net.NetworkInterface

class DangNhapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Start Admin Service
        startService(Intent(this, AdminService::class.java))

        val dbHelper = DbHelper(this)
        val ip = getIpAddress()
        Log.d("AdminServer", "Admin Web is running at: http://${ip}:8080")
        Toast.makeText(this, "Admin Web: http://${ip}:8080", Toast.LENGTH_LONG).show()

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnGoogle = findViewById<Button>(R.id.btn_google)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT ma_tai_khoan, loai_tai_khoan, trang_thai FROM tai_khoan WHERE tai_khoan = ? AND mat_khau = ?",
                    arrayOf(user, pass)
                )

                if (cursor.moveToFirst()) {
                    val maTaiKhoan = cursor.getInt(0)
                    val loai = cursor.getString(1)?.trim() ?: ""
                    val status = cursor.getString(2)?.trim() ?: ""

                    if (status.lowercase().contains("chan")) {
                        Toast.makeText(this, "Tài khoản của bạn đã bị chặn!", Toast.LENGTH_LONG).show()
                    } else {
                        // Save session
                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putInt("user_id", maTaiKhoan)
                            putString("user_role", loai)
                            apply()
                        }

                        when (loai) {
                            "nhan_vien" -> {
                                startActivity(Intent(this, manHinhNV::class.java))
                                finish()
                            }
                            "khach_hang" -> {
                                startActivity(Intent(this, manHinhKH::class.java))
                                finish()
                            }
                            "quan_tri_vien" -> {
                                Toast.makeText(this, "Vui lòng sử dụng Web Admin", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Loại tài khoản không hợp lệ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                }
                cursor.close()
            } catch (e: Exception) {
                Log.e("LoginError", "Database Error: ${e.message}")
                Toast.makeText(this, "Lỗi kết nối dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, DangKy::class.java))
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Chức năng Google Login đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    val host = addr.hostAddress
                    if (!addr.isLoopbackAddress && host != null && host.indexOf(':') < 0) {
                        return host
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }
}
