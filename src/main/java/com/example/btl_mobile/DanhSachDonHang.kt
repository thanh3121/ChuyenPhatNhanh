package com.example.btl_mobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class DanhSachDonHang : Fragment() {

    private var isPending: Boolean = true
    private lateinit var rvOrders: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var orderList = mutableListOf<DonVanChuyen>()

    companion object {
        fun newInstance(isPending: Boolean): DanhSachDonHang {
            val fragment = DanhSachDonHang()
            val args = Bundle()
            args.putBoolean("is_pending", isPending)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPending = arguments?.getBoolean("is_pending") ?: true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_list, container, false)
        rvOrders = view.findViewById(R.id.rv_order_list)
        swipeRefresh = view.findViewById(R.id.swipe_refresh_orders)
        
        rvOrders.layoutManager = LinearLayoutManager(requireContext())
        
        loadOrders()
        
        swipeRefresh.setOnRefreshListener {
            loadOrders()
        }
        
        return view
    }

    fun loadOrders() {
        val dbHelper = DbHelper(requireContext())
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        
        // Need to get ma_nhan_vien if it's accepted tab
        var maNhanVien: String? = null
        if (!isPending && userId != -1) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT ma_nhan_vien FROM nhan_vien WHERE ma_tai_khoan = ?", arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                maNhanVien = cursor.getString(0)
            }
            cursor.close()
        }

        val db = dbHelper.readableDatabase
        val query = if (isPending) {
            "SELECT * FROM don_van_chuyen WHERE trang_thai = 'Dang xu ly' AND (ma_nhan_vien_giao IS NULL OR ma_nhan_vien_giao = '') ORDER BY ngay_tao DESC"
        } else {
            "SELECT * FROM don_van_chuyen WHERE ma_nhan_vien_giao = ? ORDER BY ngay_tao DESC"
        }
        val selectionArgs = if (isPending) null else arrayOf(maNhanVien ?: "")

        val cursor = db.rawQuery(query, selectionArgs)
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
        
        rvOrders.adapter = DanhSachDonAdapter(orderList) { order ->
            showOrderDetail(order)
        }
        swipeRefresh.isRefreshing = false
    }

    private fun showOrderDetail(order: DonVanChuyen) {
        // We'll implement a BottomSheet or Dialog for details
        val dialog = ChiTietDonDialog.newInstance(order, isPending)
        dialog.show(childFragmentManager, "order_detail")
    }
}
