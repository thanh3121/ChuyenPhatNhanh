package com.example.btl_mobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlin.math.*

class TaoDonHang : Fragment() {

    private lateinit var ivItemImage: ImageView
    private lateinit var etDesc: EditText
    private lateinit var etWeight: EditText
    private lateinit var rgGoiCuoc: RadioGroup
    private lateinit var spPickup: Spinner
    private lateinit var etDeliveryManual: EditText
    private lateinit var tvDelivery: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvTotal: TextView
    private lateinit var etRecipientName: EditText
    private lateinit var etRecipientPhone: EditText
    private lateinit var cbCod: CheckBox
    private lateinit var cbInsurance: CheckBox
    private lateinit var cbSpecialPack: CheckBox
    private lateinit var etNote: EditText

    private var imageUri: Uri? = null
    private var pickupLatLng: LatLng? = null
    private var selectedPickupAddressId: Int? = null
    private var deliveryLatLng: LatLng? = null
    private var distanceKm: Double = 0.0
    private var totalPrice: Double = 0.0

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val fileSize = requireContext().contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0
            if (fileSize > 15 * 1024 * 1024) {
                Toast.makeText(requireContext(), "Ảnh quá lớn (tối đa 15MB)", Toast.LENGTH_SHORT).show()
            } else {
                val localUri = saveImageToInternalStorage(uri)
                if (localUri != null) {
                    imageUri = localUri
                    ivItemImage.setImageURI(localUri)
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val fileName = "item_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(requireContext().filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            val address = result.data?.getStringExtra("address") ?: ""
            deliveryLatLng = LatLng(lat, lng)
            tvDelivery.text = address
            calculateDistanceAndPrice()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)
        
        initViews(view)
        setupListeners(view)
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPickupAddresses()
    }

    private fun initViews(view: View) {
        ivItemImage = view.findViewById(R.id.iv_item_image)
        etDesc = view.findViewById(R.id.et_order_desc)
        etWeight = view.findViewById(R.id.et_order_weight)
        rgGoiCuoc = view.findViewById(R.id.rg_goi_cuoc)
        spPickup = view.findViewById(R.id.sp_pickup_address)
        etDeliveryManual = view.findViewById(R.id.et_delivery_detail_manual)
        tvDelivery = view.findViewById(R.id.tv_delivery_address)
        tvDistance = view.findViewById(R.id.tv_distance)
        tvTotal = view.findViewById(R.id.tv_total_price)
        etRecipientName = view.findViewById(R.id.et_recipient_name)
        etRecipientPhone = view.findViewById(R.id.et_recipient_phone)
        cbCod = view.findViewById(R.id.cb_cod)
        cbInsurance = view.findViewById(R.id.cb_insurance)
        cbSpecialPack = view.findViewById(R.id.cb_special_pack)
        etNote = view.findViewById(R.id.et_order_note)
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.btn_pick_image).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        view.findViewById<Button>(R.id.btn_pick_delivery_map).setOnClickListener {
            val intent = Intent(requireContext(), ChonViTri_Map::class.java)
            pickupLatLng?.let {
                intent.putExtra("initial_lat", it.latitude)
                intent.putExtra("initial_lng", it.longitude)
            }
            mapPickerLauncher.launch(intent)
        }

        view.findViewById<View>(R.id.btn_add_pickup_address).setOnClickListener {
            startActivity(Intent(requireContext(), ThemDiaChiActivity::class.java))
        }

        rgGoiCuoc.setOnCheckedChangeListener { _, _ -> calculateDistanceAndPrice() }
        
        etWeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateDistanceAndPrice()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        cbCod.setOnCheckedChangeListener { _, _ -> calculateDistanceAndPrice() }
        cbInsurance.setOnCheckedChangeListener { _, _ -> calculateDistanceAndPrice() }
        cbSpecialPack.setOnCheckedChangeListener { _, _ -> calculateDistanceAndPrice() }

        view.findViewById<Button>(R.id.btn_create_order).setOnClickListener {
            submitOrder()
        }
    }

    private fun loadPickupAddresses() {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            val cusCursor = db.rawQuery("SELECT ma_khach_hang FROM khach_hang WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cusCursor.moveToFirst()) {
                val maCus = cusCursor.getInt(0)
                val cursor = db.rawQuery("SELECT ma_dia_chi, dia_chi_chi_tiet, vi_do, kinh_do FROM dia_chi_nhan WHERE ma_khach_hang = ?", arrayOf(maCus.toString()))
                val addresses = mutableListOf<String>()
                val latLngs = mutableListOf<LatLng?>()
                val ids = mutableListOf<Int>()
                
                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(0))
                    addresses.add(cursor.getString(1))
                    latLngs.add(LatLng(cursor.getDouble(2), cursor.getDouble(3)))
                }
                cursor.close()

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, addresses)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPickup.adapter = adapter
                
                spPickup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position < latLngs.size) {
                            pickupLatLng = latLngs[position]
                            selectedPickupAddressId = ids[position]
                            calculateDistanceAndPrice()
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Force update if addresses exist
                if (latLngs.isNotEmpty() && pickupLatLng == null) {
                    pickupLatLng = latLngs[0]
                    selectedPickupAddressId = ids[0]
                    calculateDistanceAndPrice()
                }
            }
            cusCursor.close()
        }
    }

    private fun calculateDistanceAndPrice() {
        val locale = Locale.getDefault()
        
        if (pickupLatLng == null || deliveryLatLng == null) {
            tvDistance.text = "Quãng đường: -- km"
            tvTotal.text = "TỔNG THANH TOÁN: 0đ"
            return
        }

        distanceKm = calculateDistance(pickupLatLng!!, deliveryLatLng!!)
        tvDistance.text = "Quãng đường: ${String.format(locale, "%.2f", distanceKm)} km"

        val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
        
        // Base price logic
        var calculatedPrice = 15000.0 // Base
        
        val selectedGoiCuoc = when (rgGoiCuoc.checkedRadioButtonId) {
            R.id.rb_noi_tinh -> "Noi tinh"
            R.id.rb_noi_mien -> "Noi mien"
            R.id.rb_lien_mien -> "Lien mien"
            else -> "Noi tinh"
        }

        if (selectedGoiCuoc != "Noi tinh" && distanceKm > 5.0) {
            calculatedPrice += (distanceKm - 5.0) * 3000.0
        }

        if (weight > 1.0) {
            calculatedPrice += ceil((weight - 1.0) / 0.5) * 2500.0
        }

        if (cbInsurance.isChecked) calculatedPrice += 9900.0
        if (cbSpecialPack.isChecked) calculatedPrice += 5000.0

        totalPrice = calculatedPrice
        tvTotal.text = "TỔNG THANH TOÁN: ${String.format(locale, "%,.0f", totalPrice)}đ"
    }

    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371 // Radius of earth in km
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun submitOrder() {
        val desc = etDesc.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()
        val recipientName = etRecipientName.text.toString().trim()
        val recipientPhone = etRecipientPhone.text.toString().trim()
        val manualAddr = etDeliveryManual.text.toString().trim()

        if (desc.isBlank() || weightStr.isBlank() || recipientName.isBlank() || recipientPhone.isBlank() || deliveryLatLng == null || pickupLatLng == null) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDouble()
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.writableDatabase
            val cusCursor = db.rawQuery("SELECT ma_khach_hang, ho_ten, so_dien_thoai FROM khach_hang WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cusCursor.moveToFirst()) {
                val maCus = cusCursor.getInt(0)
                val tenCus = cusCursor.getString(1)
                val sdtCus = cusCursor.getString(2)

                val fullDelAddr = if (manualAddr.isNotEmpty()) "$manualAddr (${tvDelivery.text})" else tvDelivery.text.toString()

                val values = android.content.ContentValues().apply {
                    put("ma_khach_hang", maCus)
                    put("ten_khach_hang", tenCus)
                    put("so_dien_thoai", sdtCus)
                    put("ma_goi_cuoc", when (rgGoiCuoc.checkedRadioButtonId) {
                        R.id.rb_noi_tinh -> 1
                        R.id.rb_noi_mien -> 2
                        R.id.rb_lien_mien -> 3
                        else -> 1
                    })
                    put("ma_dia_chi_nhan", selectedPickupAddressId)
                    put("dia_chi_giao", fullDelAddr)
                    put("vi_do_giao", deliveryLatLng!!.latitude)
                    put("kinh_do_giao", deliveryLatLng!!.longitude)
                    put("ten_nguoi_nhan", recipientName)
                    put("sdt_nguoi_nhan", recipientPhone)
                    put("anh_mat_hang", imageUri?.toString())
                    put("mo_ta", desc)
                    put("khoi_luong", weight)
                    put("quang_duong_km", distanceKm)
                    put("co_cod", if (cbCod.isChecked) 1 else 0)
                    put("co_bao_hiem", if (cbInsurance.isChecked) 1 else 0)
                    put("co_dong_goi_dac_biet", if (cbSpecialPack.isChecked) 1 else 0)
                    put("tong_thanh_toan", totalPrice)
                    put("ghi_chu", etNote.text.toString())
                    put("trang_thai", "Dang xu ly")
                }

                val rowId = db.insert("don_van_chuyen", null, values)
                if (rowId != -1L) {
                    Toast.makeText(requireContext(), "Tạo đơn thành công!", Toast.LENGTH_SHORT).show()
                    activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_history
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi tạo đơn", Toast.LENGTH_SHORT).show()
                }
            }
            cusCursor.close()
        }
    }
}