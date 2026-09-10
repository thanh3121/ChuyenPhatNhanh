package com.example.btl_mobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ThemDiaChiActivity : AppCompatActivity() {

    private lateinit var spMien: Spinner
    private lateinit var spTinh: Spinner
    private lateinit var spXa: Spinner
    private lateinit var etDetail: EditText
    private lateinit var tvLatLng: TextView
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    private val mienList = listOf("Miền Bắc", "Miền Trung", "Miền Nam")
    private val tinhMap = mapOf(
        "Miền Bắc" to listOf(
            "Hà Nội", "Hải Phòng", "Cao Bằng", "Điện Biên", "Lai Châu", "Lạng Sơn", 
            "Lào Cai", "Phú Thọ", "Quảng Ninh", "Sơn La", "Thái Nguyên", "Tuyên Quang", 
            "Bắc Ninh", "Hưng Yên"
        ),
        "Miền Trung" to listOf(
            "Đà Nẵng", "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Trị", "Huế", 
            "Quảng Ngãi", "Gia Lai", "Khánh Hòa", "Lâm Đồng", "Đắk Lắk"
        ),
        "Miền Nam" to listOf(
            "Thành phố Hồ Chí Minh", "Cần Thơ", "Đồng Nai", "Tây Ninh", 
            "Bà Rịa – Vũng Tàu", "Bình Phước", "An Giang", "Đồng Tháp", 
            "Vĩnh Long", "Cà Mau"
        )
    )

    // Dữ liệu mẫu cho Xã/Phường/Quận tương ứng với các Tỉnh/Thành phố
    private fun getXaList(tinh: String): List<String> {
        return when (tinh) {
            "Hà Nội" -> listOf("Quận Ba Đình", "Quận Hoàn Kiếm", "Quận Tây Hồ", "Quận Cầu Giấy", "Quận Đống Đa", "Quận Hai Bà Trưng", "Huyện Đông Anh", "Huyện Gia Lâm")
            "Hải Phòng" -> listOf("Quận Hồng Bàng", "Quận Ngô Quyền", "Quận Lê Chân", "Quận Hải An", "Huyện Thủy Nguyên")
            "Đà Nẵng" -> listOf("Quận Hải Châu", "Quận Thanh Khê", "Quận Sơn Trà", "Quận Ngũ Hành Sơn", "Quận Liên Chiểu")
            "Thành phố Hồ Chí Minh" -> listOf("Quận 1", "Quận 3", "Quận 5", "Quận 10", "Quận Bình Thạnh", "Thành phố Thủ Đức", "Huyện Hóc Môn")
            "Cần Thơ" -> listOf("Quận Ninh Kiều", "Quận Bình Thủy", "Quận Cái Răng", "Huyện Phong Điền")
            else -> listOf("Phường Trung Tâm", "Phường 1", "Phường 2", "Xã Nội Thành", "Thị trấn Huyện")
        }
    }

    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedLat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            selectedLng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            tvLatLng.text = "Vị trí: $selectedLat, $selectedLng"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)

        spMien = findViewById(R.id.sp_mien)
        spTinh = findViewById(R.id.sp_tinh)
        spXa = findViewById(R.id.sp_xa)
        etDetail = findViewById(R.id.et_address_detail)
        tvLatLng = findViewById(R.id.tv_latlng)

        setupSpinners()

        findViewById<Button>(R.id.btn_pick_map).setOnClickListener {
            mapPickerLauncher.launch(Intent(this, ChonViTri_Map::class.java))
        }

        findViewById<Button>(R.id.btn_save_address).setOnClickListener {
            saveAddress()
        }
    }

    private fun setupSpinners() {
        // Setup Miền
        val mienAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mienList)
        mienAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMien.adapter = mienAdapter

        spMien.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMien = mienList[position]
                updateTinhSpinner(selectedMien)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Listener cho Tỉnh để cập nhật Xã
        spTinh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTinh = spTinh.selectedItem.toString()
                updateXaSpinner(selectedTinh)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateTinhSpinner(mien: String) {
        val tinhList = tinhMap[mien] ?: emptyList()
        val tinhAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tinhList)
        tinhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTinh.adapter = tinhAdapter
    }

    private fun updateXaSpinner(tinh: String) {
        val listXa = getXaList(tinh)
        val xaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listXa)
        xaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spXa.adapter = xaAdapter
    }

    private fun saveAddress() {
        val detail = etDetail.text.toString().trim()
        if (detail.isBlank() || selectedLat == 0.0) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin và chọn vị trí", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DbHelper(this)
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.writableDatabase
            val cusCursor = db.rawQuery("SELECT ma_khach_hang FROM khach_hang WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cusCursor.moveToFirst()) {
                val maCus = cusCursor.getInt(0)
                val values = android.content.ContentValues().apply {
                    put("ma_khach_hang", maCus)
                    put("mien", spMien.selectedItem.toString())
                    put("tinh_thanh", spTinh.selectedItem.toString())
                    put("xa_phuong", spXa.selectedItem.toString())
                    put("dia_chi_chi_tiet", detail)
                    put("vi_do", selectedLat)
                    put("kinh_do", selectedLng)
                }
                db.insert("dia_chi_nhan", null, values)
                Toast.makeText(this, "Lưu địa chỉ thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            cusCursor.close()
        }
    }
}
