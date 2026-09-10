package com.example.btl_mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QuanLyDiaChi : AppCompatActivity() {

    private lateinit var rvAddresses: RecyclerView
    private lateinit var adapter: ChuyenDoiDiaChi
    private var addressList = mutableListOf<Address>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_management)

        rvAddresses = findViewById(R.id.rv_addresses)
        rvAddresses.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btn_add_address).setOnClickListener {
            startActivity(Intent(this, ThemDiaChiActivity::class.java))
        }

        loadAddresses()
    }

    override fun onResume() {
        super.onResume()
        loadAddresses()
    }

    private fun loadAddresses() {
        val dbHelper = DbHelper(this)
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            val cusCursor = db.rawQuery("SELECT ma_khach_hang FROM khach_hang WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cusCursor.moveToFirst()) {
                val maCus = cusCursor.getInt(0)
                val cursor = db.rawQuery("SELECT * FROM dia_chi_nhan WHERE ma_khach_hang = ?", arrayOf(maCus.toString()))
                
                addressList.clear()
                while (cursor.moveToNext()) {
                    addressList.add(Address(
                        maDiaChi = cursor.getInt(cursor.getColumnIndexOrThrow("ma_dia_chi")),
                        maKhachHang = cursor.getInt(cursor.getColumnIndexOrThrow("ma_khach_hang")),
                        mien = cursor.getString(cursor.getColumnIndexOrThrow("mien")),
                        tinhThanh = cursor.getString(cursor.getColumnIndexOrThrow("tinh_thanh")),
                        xaPhuong = cursor.getString(cursor.getColumnIndexOrThrow("xa_phuong")),
                        diaChiChiTiet = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi_chi_tiet")),
                        viDo = cursor.getDouble(cursor.getColumnIndexOrThrow("vi_do")),
                        kinhDo = cursor.getDouble(cursor.getColumnIndexOrThrow("kinh_do"))
                    ))
                }
                cursor.close()
                adapter = ChuyenDoiDiaChi(addressList)
                rvAddresses.adapter = adapter
            }
            cusCursor.close()
        }
    }
}
