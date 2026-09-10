package com.example.btl_mobile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class ChiTietDonDialog : DialogFragment() {

    private lateinit var order: DonVanChuyen
    private var isPending: Boolean = true

    companion object {
        fun newInstance(order: DonVanChuyen, isPending: Boolean): ChiTietDonDialog {
            val dialog = ChiTietDonDialog()
            dialog.order = order
            dialog.isPending = isPending
            return dialog
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_order_detail, container, false)
        
        initViews(view)
        
        return view
    }

    private fun initViews(view: View) {
        view.findViewById<TextView>(R.id.tv_dlg_id).text = "Mã đơn: #${order.maDon}"
        view.findViewById<TextView>(R.id.tv_dlg_customer).text = "Khách hàng: ${order.tenKhachHang}"
        view.findViewById<TextView>(R.id.tv_dlg_cus_phone).text = "SĐT khách: ${order.soDienThoai}"
        view.findViewById<TextView>(R.id.tv_dlg_delivery).text = "Địa chỉ giao: ${order.diaChiGiao}"
        
        val tvPickup = view.findViewById<TextView>(R.id.tv_dlg_pickup)
        if (order.maDiaChiNhan != null && order.maDiaChiNhan != 0) {
            tvPickup.text = "Địa chỉ lấy: ${getPickupAddress(order.maDiaChiNhan!!)}"
        } else {
            tvPickup.text = "Địa chỉ lấy: N/A"
        }
        
        view.findViewById<TextView>(R.id.tv_dlg_recipient).text = "Người nhận: ${order.tenNguoiNhan}"
        view.findViewById<TextView>(R.id.tv_dlg_rec_phone).text = "SĐT nhận: ${order.sdtNguoiNhan}"
        
        val ivImage = view.findViewById<ImageView>(R.id.iv_dlg_image)
        if (!order.anhMatHang.isNullOrEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(order.anhMatHang))
            } catch (e: Exception) {
                ivImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        view.findViewById<TextView>(R.id.tv_dlg_desc).text = "Mô tả: ${order.moTa}"
        view.findViewById<TextView>(R.id.tv_dlg_weight).text = "Khối lượng: ${order.khoiLuong} kg"
        
        val options = mutableListOf<String>()
        if (order.coCod == 1) options.add("COD")
        if (order.coBaoHiem == 1) options.add("Bảo hiểm")
        if (order.coDongGoiDacBiet == 1) options.add("Đóng gói ĐB")
        view.findViewById<TextView>(R.id.tv_dlg_options).text = "Tùy chọn: ${if (options.isEmpty()) "Không" else options.joinToString(", ")}"
        
        view.findViewById<TextView>(R.id.tv_dlg_payment).text = "Tổng tiền: ${String.format(Locale.getDefault(), "%,.0f", order.tongThanhToan)}đ"
        view.findViewById<TextView>(R.id.tv_dlg_status).text = "Trạng thái: ${order.trangThai}"

        val btnAccept = view.findViewById<Button>(R.id.btn_accept_order)
        val spStatus = view.findViewById<Spinner>(R.id.sp_update_status)
        val btnUpdate = view.findViewById<Button>(R.id.btn_update_order)
        val tilReason = view.findViewById<TextInputLayout>(R.id.til_fail_reason)
        val etReason = view.findViewById<EditText>(R.id.et_fail_reason)

        if (isPending) {
            btnAccept.visibility = View.VISIBLE
            btnAccept.setOnClickListener { acceptOrder() }
        } else {
            if (order.trangThai != "Thanh cong" && order.trangThai != "That bai" && order.trangThai != "Da huy") {
                spStatus.visibility = View.VISIBLE
                btnUpdate.visibility = View.VISIBLE
                
                val statusOptions = listOf("Cho lay hang", "Cho giao hang", "Thanh cong", "That bai")
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spStatus.adapter = adapter
                
                // Set current selection if applicable
                val currentIndex = statusOptions.indexOf(order.trangThai)
                if (currentIndex != -1) spStatus.setSelection(currentIndex)

                spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                        tilReason.visibility = if (statusOptions[position] == "That bai") View.VISIBLE else View.GONE
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                btnUpdate.setOnClickListener {
                    val newStatus = spStatus.selectedItem.toString()
                    val reason = if (newStatus == "That bai") etReason.text.toString().trim() else ""
                    if (newStatus == "That bai" && reason.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập lí do thất bại", Toast.LENGTH_SHORT).show()
                    } else {
                        updateOrderStatus(newStatus, reason)
                    }
                }
            }
        }

        view.findViewById<Button>(R.id.btn_close_dlg).setOnClickListener { dismiss() }
    }

    private fun acceptOrder() {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.writableDatabase
            val cursor = db.rawQuery("SELECT ma_nhan_vien FROM nhan_vien WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                val maNV = cursor.getString(0)
                val values = android.content.ContentValues().apply {
                    put("ma_nhan_vien_giao", maNV)
                    put("trang_thai", "Cho lay hang")
                    put("ngay_cap_nhat", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date()))
                }
                val rows = db.update("don_van_chuyen", values, "ma_don = ?", arrayOf(order.maDon.toString()))
                if (rows > 0) {
                    Toast.makeText(requireContext(), "Đã nhận đơn hàng", Toast.LENGTH_SHORT).show()
                    (parentFragment as? DanhSachDonHang)?.loadOrders()
                    dismiss()
                }
            }
            cursor.close()
        }
    }

    private fun updateOrderStatus(newStatus: String, reason: String) {
        val dbHelper = DbHelper(requireContext())
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("trang_thai", newStatus)
            put("li_do", reason)
            put("ngay_cap_nhat", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date()))
        }
        val rows = db.update("don_van_chuyen", values, "ma_don = ?", arrayOf(order.maDon.toString()))
        if (rows > 0) {
            Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            (parentFragment as? DanhSachDonHang)?.loadOrders()
            dismiss()
        }
    }

    private fun getPickupAddress(maDiaChi: Int): String {
        val dbHelper = DbHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT dia_chi_chi_tiet, xa_phuong, tinh_thanh, mien FROM dia_chi_nhan WHERE ma_dia_chi = ?", arrayOf(maDiaChi.toString()))
        var address = "N/A"
        if (cursor.moveToFirst()) {
            val chiTiet = cursor.getString(0) ?: ""
            val xa = cursor.getString(1) ?: ""
            val tinh = cursor.getString(2) ?: ""
            val mien = cursor.getString(3) ?: ""
            
            val parts = listOf(chiTiet, xa, tinh, mien).filter { it.isNotBlank() }
            address = if (parts.isNotEmpty()) parts.joinToString(", ") else "N/A"
        }
        cursor.close()
        return address
    }
}
