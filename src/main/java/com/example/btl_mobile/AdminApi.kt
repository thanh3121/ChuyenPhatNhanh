package com.example.btl_mobile

import android.content.ContentValues
import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class ShippingRate(
    val ma_goi_cuoc: Int? = null,
    val ten_tuyen: String = "",
    val cuoc_co_ban: Double? = 0.0,
    val km_mien_phi: Double? = 0.0,
    val don_gia_vuot_km: Double? = 0.0,
    val khoi_luong_co_ban: Double? = 0.0,
    val buoc_khoi_luong: Double? = 0.0,
    val don_gia_vuot_khoi_luong: Double? = 0.0,
    val phi_cod: Double? = 0.0,
    val phi_bao_hiem: Double? = 0.0,
    val phi_dong_goi: Double? = 0.0,
    val tg_giao_toi_thieu: Int? = 0,
    val tg_giao_toi_da: Int? = 0,
    val ghi_chu: String? = ""
)

@Serializable
data class Account(
    val ma_tai_khoan: Int? = null,
    val tai_khoan: String,
    val mat_khau: String? = null,
    val loai_tai_khoan: String = "khach_hang",
    val trang_thai: String = "Hoat dong",
    val ho_ten: String? = null,
    val so_dien_thoai: String? = null,
    val dia_chi: String? = null,
    val ma_nhan_vien: String? = null
)

@Serializable
data class Employee(
    val ma_nhan_vien: String = "",
    val ho_ten: String = "",
    val que_quan: String? = "",
    val so_dien_thoai: String? = "",
    val cccd: String? = "",
    val ngay_vao_lam: String? = null,
    val trang_thai: String = "Dang hoat dong"
)

@Serializable
data class ShippingOrder(
    val ma_don: Int? = null,
    val ten_khach_hang: String? = "",
    val so_dien_thoai: String? = "",
    val ten_goi_cuoc: String? = "",
    val dia_chi_lay: String? = "",
    val dia_chi_giao: String? = "",
    val ten_nguoi_nhan: String? = "",
    val sdt_nguoi_nhan: String? = "",
    val anh_mat_hang: String? = "",
    val mo_ta: String? = "",
    val khoi_luong: Double? = 0.0,
    val co_cod: Int? = 0,
    val co_bao_hiem: Int? = 0,
    val co_dong_goi_dac_biet: Int? = 0,
    val tong_thanh_toan: Double? = 0.0,
    val ghi_chu: String? = "",
    val ma_nhan_vien_giao: String? = "",
    val trang_thai: String = "Dang xu ly",
    val li_do: String? = ""
)

@Serializable
data class ChartData(
    val label: String,
    val value: Double
)

@Serializable
data class DashboardStats(
    val totalOrders: Int,
    val totalRevenue: Double,
    val ordersByStatus: List<ChartData>,
    val revenueByRoute: List<ChartData>,
    val popularService: List<ChartData>,
    val topEmployees: List<Employee>
)

class AdminApi(private val dbHelper: DbHelper) {

    // --- Price Management ---
    fun getAllRates(): List<ShippingRate> {
        val rates = mutableListOf<ShippingRate>()
        val db = dbHelper.readableDatabase
        try {
            val cursor = db.rawQuery("SELECT * FROM goi_cuoc", null)
            cursor.use {
                while (it.moveToNext()) {
                    rates.add(ShippingRate(
                        ma_goi_cuoc = it.getInt(it.getColumnIndexOrThrow("ma_goi_cuoc")),
                        ten_tuyen = it.getString(it.getColumnIndexOrThrow("ten_tuyen")) ?: "Unknown",
                        cuoc_co_ban = it.getDouble(it.getColumnIndexOrThrow("cuoc_co_ban")),
                        km_mien_phi = it.getDouble(it.getColumnIndexOrThrow("km_mien_phi")),
                        don_gia_vuot_km = it.getDouble(it.getColumnIndexOrThrow("don_gia_vuot_km")),
                        khoi_luong_co_ban = it.getDouble(it.getColumnIndexOrThrow("khoi_luong_co_ban")),
                        buoc_khoi_luong = it.getDouble(it.getColumnIndexOrThrow("buoc_khoi_luong")),
                        don_gia_vuot_khoi_luong = it.getDouble(it.getColumnIndexOrThrow("don_gia_vuot_khoi_luong")),
                        phi_cod = it.getDouble(it.getColumnIndexOrThrow("phi_cod")),
                        phi_bao_hiem = it.getDouble(it.getColumnIndexOrThrow("phi_bao_hiem")),
                        phi_dong_goi = it.getDouble(it.getColumnIndexOrThrow("phi_dong_goi")),
                        tg_giao_toi_thieu = it.getInt(it.getColumnIndexOrThrow("tg_giao_toi_thieu")),
                        tg_giao_toi_da = it.getInt(it.getColumnIndexOrThrow("tg_giao_toi_da")),
                        ghi_chu = it.getString(it.getColumnIndexOrThrow("ghi_chu")) ?: ""
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("AdminApi", "Error getAllRates: ${e.message}")
        }
        return rates
    }

    fun updateRate(rate: ShippingRate): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("cuoc_co_ban", rate.cuoc_co_ban)
            put("km_mien_phi", rate.km_mien_phi)
            put("don_gia_vuot_km", rate.don_gia_vuot_km)
            put("khoi_luong_co_ban", rate.khoi_luong_co_ban)
            put("buoc_khoi_luong", rate.buoc_khoi_luong)
            put("don_gia_vuot_khoi_luong", rate.don_gia_vuot_khoi_luong)
            put("phi_cod", rate.phi_cod)
            put("phi_bao_hiem", rate.phi_bao_hiem)
            put("phi_dong_goi", rate.phi_dong_goi)
            put("tg_giao_toi_thieu", rate.tg_giao_toi_thieu)
            put("tg_giao_toi_da", rate.tg_giao_toi_da)
            put("ghi_chu", rate.ghi_chu)
        }
        return db.update("goi_cuoc", values, "ma_goi_cuoc = ?", arrayOf(rate.ma_goi_cuoc.toString())) > 0
    }

    // --- Account Management ---
    fun getAllAccounts(type: String): List<Account> {
        val accounts = mutableListOf<Account>()
        val db = dbHelper.readableDatabase
        try {
            val query = if (type == "khach_hang") {
                """
                    SELECT t.*, k.ho_ten, k.so_dien_thoai, k.dia_chi 
                    FROM tai_khoan t 
                    LEFT JOIN khach_hang k ON t.ma_tai_khoan = k.ma_tai_khoan 
                    WHERE t.loai_tai_khoan = 'khach_hang'
                """
            } else {
                """
                    SELECT t.*, n.ma_nhan_vien, n.ho_ten 
                    FROM tai_khoan t 
                    LEFT JOIN nhan_vien n ON t.ma_tai_khoan = n.ma_tai_khoan 
                    WHERE t.loai_tai_khoan = 'nhan_vien'
                """
            }
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {
                    accounts.add(Account(
                        ma_tai_khoan = cursor.getInt(cursor.getColumnIndexOrThrow("ma_tai_khoan")),
                        tai_khoan = cursor.getString(cursor.getColumnIndexOrThrow("tai_khoan")) ?: "",
                        loai_tai_khoan = cursor.getString(cursor.getColumnIndexOrThrow("loai_tai_khoan")) ?: "khach_hang",
                        trang_thai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")) ?: "Hoat dong",
                        ho_ten = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                        so_dien_thoai = if (type == "khach_hang") cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")) else null,
                        dia_chi = if (type == "khach_hang") cursor.getString(cursor.getColumnIndexOrThrow("dia_chi")) else null,
                        ma_nhan_vien = if (type == "nhan_vien") cursor.getString(cursor.getColumnIndexOrThrow("ma_nhan_vien")) else null
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("AdminApi", "Error getAllAccounts: ${e.message}")
        }
        return accounts
    }

    fun toggleAccountStatus(ma_tai_khoan: Int, currentStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val normalizedStatus = currentStatus.lowercase()
        val newStatus = if (normalizedStatus.contains("hoat")) "Bi chan" else "Hoat dong"
        val values = ContentValues().apply { put("trang_thai", newStatus) }
        return db.update("tai_khoan", values, "ma_tai_khoan = ?", arrayOf(ma_tai_khoan.toString())) > 0
    }

    fun addEmployeeAccount(acc: Account): Long {
        val db = dbHelper.writableDatabase
        
        // 1. Check if employee exists
        val cursor = db.rawQuery("SELECT 1 FROM nhan_vien WHERE ma_nhan_vien = ?", arrayOf(acc.ma_nhan_vien))
        val exists = cursor.moveToFirst()
        cursor.close()
        
        if (!exists) return -1L // Employee not found
        
        db.beginTransaction()
        try {
            // 2. Insert account
            val values = ContentValues().apply {
                put("tai_khoan", acc.tai_khoan)
                put("mat_khau", acc.mat_khau ?: "123456") // Default password if null
                put("loai_tai_khoan", "nhan_vien")
                put("trang_thai", "Hoat dong")
            }
            val maTaiKhoan = db.insert("tai_khoan", null, values)
            
            if (maTaiKhoan != -1L) {
                // 3. Link account to employee
                val nvValues = ContentValues().apply {
                    put("ma_tai_khoan", maTaiKhoan)
                }
                val rows = db.update("nhan_vien", nvValues, "ma_nhan_vien = ?", arrayOf(acc.ma_nhan_vien))
                if (rows > 0) {
                    db.setTransactionSuccessful()
                    return maTaiKhoan
                }
            }
            return -1L
        } finally {
            db.endTransaction()
        }
    }

    fun updateEmployeeAccount(acc: Account): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Update Username/Password
            val values = ContentValues().apply {
                put("tai_khoan", acc.tai_khoan)
                if (!acc.mat_khau.isNullOrBlank()) {
                    put("mat_khau", acc.mat_khau)
                }
            }
            db.update("tai_khoan", values, "ma_tai_khoan = ?", arrayOf(acc.ma_tai_khoan.toString()))

            // 2. Re-assign employee if ma_nhan_vien is provided
            if (acc.ma_nhan_vien != null) {
                // Clear existing link for this account
                val clearValues = ContentValues().apply { putNull("ma_tai_khoan") }
                db.update("nhan_vien", clearValues, "ma_tai_khoan = ?", arrayOf(acc.ma_tai_khoan.toString()))

                // Link to new employee
                val linkValues = ContentValues().apply { put("ma_tai_khoan", acc.ma_tai_khoan) }
                db.update("nhan_vien", linkValues, "ma_nhan_vien = ?", arrayOf(acc.ma_nhan_vien))
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            Log.e("AdminApi", "Error updating employee account: ${e.message}")
            return false
        } finally {
            db.endTransaction()
        }
    }

    // --- Employee Profile Management ---
    fun getAllEmployees(): List<Employee> {
        val employees = mutableListOf<Employee>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM nhan_vien", null)
        while (cursor.moveToNext()) {
            employees.add(Employee(
                cursor.getString(cursor.getColumnIndexOrThrow("ma_nhan_vien")),
                cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")),
                cursor.getString(cursor.getColumnIndexOrThrow("que_quan")),
                cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")),
                cursor.getString(cursor.getColumnIndexOrThrow("cccd")),
                cursor.getString(cursor.getColumnIndexOrThrow("ngay_vao_lam")),
                cursor.getString(cursor.getColumnIndexOrThrow("trang_thai"))
            ))
        }
        cursor.close()
        return employees
    }

    fun upsertEmployee(emp: Employee): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("ma_nhan_vien", emp.ma_nhan_vien)
            put("ho_ten", emp.ho_ten)
            put("que_quan", emp.que_quan)
            put("so_dien_thoai", emp.so_dien_thoai)
            put("cccd", emp.cccd)
            put("trang_thai", emp.trang_thai)
        }
        val rows = db.update("nhan_vien", values, "ma_nhan_vien = ?", arrayOf(emp.ma_nhan_vien))
        return if (rows == 0) {
            db.insert("nhan_vien", null, values) != -1L
        } else true
    }

    // --- Order Management ---
    fun getOrders(status: String? = null): List<ShippingOrder> {
        val orders = mutableListOf<ShippingOrder>()
        val db = dbHelper.readableDatabase
        try {
            var query = """
                SELECT d.*, g.ten_tuyen as ten_goi_cuoc,
                       (COALESCE(dn.mien || ', ', '') || COALESCE(dn.tinh_thanh || ', ', '') || COALESCE(dn.xa_phuong || ' - ', '') || COALESCE(dn.dia_chi_chi_tiet, '')) as dia_chi_lay_full
                FROM don_van_chuyen d 
                JOIN goi_cuoc g ON d.ma_goi_cuoc = g.ma_goi_cuoc
                LEFT JOIN dia_chi_nhan dn ON d.ma_dia_chi_nhan = dn.ma_dia_chi
            """
            if (status != null) query += " WHERE d.trang_thai = '$status'"
            
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {
                    orders.add(ShippingOrder(
                        ma_don = cursor.getInt(cursor.getColumnIndexOrThrow("ma_don")),
                        ten_khach_hang = cursor.getString(cursor.getColumnIndexOrThrow("ten_khach_hang")),
                        so_dien_thoai = cursor.getString(cursor.getColumnIndexOrThrow("so_dien_thoai")),
                        ten_goi_cuoc = cursor.getString(cursor.getColumnIndexOrThrow("ten_goi_cuoc")),
                        dia_chi_lay = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi_lay_full")) ?: "Chưa xác định",
                        dia_chi_giao = cursor.getString(cursor.getColumnIndexOrThrow("dia_chi_giao")),
                        ten_nguoi_nhan = cursor.getString(cursor.getColumnIndexOrThrow("ten_nguoi_nhan")),
                        sdt_nguoi_nhan = cursor.getString(cursor.getColumnIndexOrThrow("sdt_nguoi_nhan")),
                        anh_mat_hang = cursor.getString(cursor.getColumnIndexOrThrow("anh_mat_hang")),
                        mo_ta = cursor.getString(cursor.getColumnIndexOrThrow("mo_ta")),
                        khoi_luong = cursor.getDouble(cursor.getColumnIndexOrThrow("khoi_luong")),
                        co_cod = cursor.getInt(cursor.getColumnIndexOrThrow("co_cod")),
                        co_bao_hiem = cursor.getInt(cursor.getColumnIndexOrThrow("co_bao_hiem")),
                        co_dong_goi_dac_biet = cursor.getInt(cursor.getColumnIndexOrThrow("co_dong_goi_dac_biet")),
                        tong_thanh_toan = cursor.getDouble(cursor.getColumnIndexOrThrow("tong_thanh_toan")),
                        ghi_chu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu")),
                        ma_nhan_vien_giao = cursor.getString(cursor.getColumnIndexOrThrow("ma_nhan_vien_giao")),
                        trang_thai = cursor.getString(cursor.getColumnIndexOrThrow("trang_thai")) ?: "Dang xu ly",
                        li_do = cursor.getString(cursor.getColumnIndexOrThrow("li_do"))
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("AdminApi", "Error getOrders: ${e.message}")
        }
        return orders
    }

    fun updateOrderStatus(maDon: Int, status: String, reason: String? = null): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("trang_thai", status)
            if (reason != null) put("li_do", reason)
            put("ngay_cap_nhat", "datetime('now')")
        }
        return db.update("don_van_chuyen", values, "ma_don = ?", arrayOf(maDon.toString())) > 0
    }

    // --- Statistics ---
    fun getDashboardStats(): DashboardStats {
        val db = dbHelper.readableDatabase
        var totalOrders = 0
        var totalRevenue = 0.0
        val ordersByStatus = mutableListOf<ChartData>()
        val revenueByRoute = mutableListOf<ChartData>()
        val popularService = mutableListOf<ChartData>()
        val topEmployees = mutableListOf<Employee>()

        try {
            // Total orders and revenue
            db.rawQuery("SELECT COUNT(*), SUM(tong_thanh_toan) FROM don_van_chuyen WHERE trang_thai IN ('Hoan tat', 'Thanh cong')", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    totalOrders = cursor.getInt(0)
                    totalRevenue = cursor.getDouble(1)
                }
            }

            // Orders by Status
            db.rawQuery("SELECT trang_thai, COUNT(*) FROM don_van_chuyen GROUP BY trang_thai", null).use { cursor ->
                while (cursor.moveToNext()) {
                    ordersByStatus.add(ChartData(cursor.getString(0) ?: "Unknown", cursor.getDouble(1)))
                }
            }

            // Revenue by Route
            db.rawQuery("""
                SELECT g.ten_tuyen, SUM(d.tong_thanh_toan) 
                FROM don_van_chuyen d 
                JOIN goi_cuoc g ON d.ma_goi_cuoc = g.ma_goi_cuoc 
                WHERE d.trang_thai IN ('Hoan tat', 'Thanh cong')
                GROUP BY g.ten_tuyen
            """, null).use { cursor ->
                while (cursor.moveToNext()) {
                    revenueByRoute.add(ChartData(cursor.getString(0) ?: "Unknown", cursor.getDouble(1)))
                }
            }

            // Popular Service
            db.rawQuery("""
                SELECT 
                    SUM(CASE WHEN co_cod = 1 THEN 1 ELSE 0 END) as cod, 
                    SUM(CASE WHEN co_bao_hiem = 1 THEN 1 ELSE 0 END) as ins, 
                    SUM(CASE WHEN co_dong_goi_dac_biet = 1 THEN 1 ELSE 0 END) as pack 
                FROM don_van_chuyen
            """, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    popularService.add(ChartData("COD", cursor.getDouble(0)))
                    popularService.add(ChartData("Bảo hiểm", cursor.getDouble(1)))
                    popularService.add(ChartData("Đóng gói", cursor.getDouble(2)))
                }
            }

            // Top Employees
            db.rawQuery("""
                SELECT n.*, COUNT(d.ma_don) as order_count 
                FROM nhan_vien n 
                JOIN don_van_chuyen d ON n.ma_nhan_vien = d.ma_nhan_vien_giao 
                WHERE d.trang_thai IN ('Hoan tat', 'Thanh cong')
                GROUP BY n.ma_nhan_vien 
                ORDER BY order_count DESC LIMIT 5
            """, null).use { cursor ->
                while (cursor.moveToNext()) {
                    topEmployees.add(Employee(
                        ma_nhan_vien = cursor.getString(cursor.getColumnIndexOrThrow("ma_nhan_vien")) ?: "",
                        ho_ten = cursor.getString(cursor.getColumnIndexOrThrow("ho_ten")) ?: "Unknown",
                        trang_thai = "Hoàn thành ${cursor.getInt(cursor.getColumnIndexOrThrow("order_count"))} đơn"
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("AdminApi", "Error getDashboardStats: ${e.message}")
        }

        return DashboardStats(totalOrders, totalRevenue, ordersByStatus, revenueByRoute, popularService, topEmployees)
    }
}
