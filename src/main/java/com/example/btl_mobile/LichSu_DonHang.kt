package com.example.btl_mobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LichSu_DonHang : Fragment() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var adapter: DanhSachDonAdapter
    private var orderList = mutableListOf<DonVanChuyen>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        rvOrderHistory = view.findViewById(R.id.rv_order_history)
        rvOrderHistory.layoutManager = LinearLayoutManager(requireContext())
        
        loadOrders()
        
        return view
    }

    private fun loadOrders() {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId != -1) {
            val db = dbHelper.readableDatabase
            // We need to get the ma_khach_hang from ma_tai_khoan
            val cusCursor = db.rawQuery("SELECT ma_khach_hang FROM khach_hang WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cusCursor.moveToFirst()) {
                val maKhachHang = cusCursor.getInt(0)
                
                val cursor = db.rawQuery(
                    "SELECT * FROM don_van_chuyen WHERE ma_khach_hang = ? ORDER BY ngay_tao DESC",
                    arrayOf(maKhachHang.toString())
                )
                
                orderList.clear()
                while (cursor.moveToNext()) {
                    val maDiaChiIdx = cursor.getColumnIndexOrThrow("ma_dia_chi_nhan")
                    orderList.add(DonVanChuyen(
                        maDon = cursor.getInt(cursor.getColumnIndexOrThrow("ma_don")),
                        maKhachHang = cursor.getInt(cursor.getColumnIndexOrThrow("ma_khach_hang")),
                        tenKhachHang = cursor.getString(cursor.getColumnIndexOrThrow("ten_khach_hang")),
                        soDienThoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")),
                        maGoiCuoc = cursor.getInt(cursor.getColumnIndexOrThrow("ma_goi_cuoc")),
                        maDiaChiNhan = if (cursor.isNull(maDiaChiIdx)) null else cursor.getInt(maDiaChiIdx),
                        diaChiGiao = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi_giao")),
                        tenNguoiNhan = cursor.getString(cursor.getColumnIndexOrThrow("ten_nguoi_nhan")),
                        sdtNguoiNhan = cursor.getString(cursor.getColumnIndexOrThrow("sdt_nguoi_nhan")),
                        anhMatHang = cursor.getString(cursor.getColumnIndexOrThrow("anh_mat_hang")),
                        moTa = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")),
                        khoiLuong = cursor.getDouble(cursor.getColumnIndexOrThrow("khoi_luong")),
                        coCod = cursor.getInt(cursor.getColumnIndexOrThrow("co_cod")),
                        coBaoHiem = cursor.getInt(cursor.getColumnIndexOrThrow("co_bao_hiem")),
                        coDongGoiDacBiet = cursor.getInt(cursor.getColumnIndexOrThrow("co_dong_goi_dac_biet")),
                        tongThanhToan = cursor.getDouble(cursor.getColumnIndexOrThrow("tong_thanh_toan")),
                        ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")),
                        trangThai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")),
                        ngayTao = cursor.getString(cursor.getColumnIndexOrThrow("ngay_tao"))
                    ))
                }
                cursor.close()
                
                adapter = DanhSachDonAdapter(orderList) { order ->
                    // Handle item click for details
                    showOrderDetails(order)
                }
                rvOrderHistory.adapter = adapter
            }
            cusCursor.close()
        }
    }

    private fun showOrderDetails(order: DonVanChuyen) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Chi tiết đơn #${order.maDon}")
        
        val details = """
            Trạng thái: ${order.trangThai}
            Người nhận: ${order.tenNguoiNhan}
            SĐT: ${order.sdtNguoiNhan}
            Địa chỉ giao: ${order.diaChiGiao}
            Mô tả: ${order.moTa}
            Khối lượng: ${order.khoiLuong} kg
            Tổng thanh toán: ${String.format(java.util.Locale.getDefault(), "%,.0f", order.tongThanhToan)}đ
        """.trimIndent()
        
        builder.setMessage(details)
        
        if (order.trangThai == "Dang xu ly" || order.trangThai == "Cho lay hang") {
            builder.setNegativeButton("Hủy đơn") { _, _ ->
                cancelOrder(order.maDon)
            }
        }
        
        if (order.trangThai == "Thanh cong") {
            builder.setPositiveButton("Đánh giá") { _, _ ->
                Toast.makeText(requireContext(), "Chức năng đánh giá đang phát triển", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNeutralButton("Đóng", null)
        builder.show()
    }

    private fun cancelOrder(maDon: Int) {
        val dbHelper = DbHelper(requireContext())
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("trang_thai", "Da huy")
        }
        val rows = db.update("don_van_chuyen", values, "ma_don = ?", arrayOf(maDon.toString()))
        if (rows > 0) {
            Toast.makeText(requireContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show()
            loadOrders() // Refresh list
        } else {
            Toast.makeText(requireContext(), "Hủy đơn thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}
