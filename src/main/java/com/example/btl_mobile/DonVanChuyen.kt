package com.example.btl_mobile

data class DonVanChuyen(
    val maDon: Int,
    val maKhachHang: Int,
    val tenKhachHang: String?,
    val soDienThoai: String?,
    val maGoiCuoc: Int,
    val maDiaChiNhan: Int?,
    val diaChiGiao: String?,
    val tenNguoiNhan: String,
    val sdtNguoiNhan: String,
    val anhMatHang: String?,
    val moTa: String?,
    val khoiLuong: Double,
    val coCod: Int,
    val coBaoHiem: Int,
    val coDongGoiDacBiet: Int,
    val tongThanhToan: Double,
    val ghiChu: String?,
    val trangThai: String,
    val ngayTao: String
)

data class Address(
    val maDiaChi: Int,
    val maKhachHang: Int,
    val mien: String?,
    val tinhThanh: String?,
    val xaPhuong: String?,
    val diaChiChiTiet: String?,
    val viDo: Double,
    val kinhDo: Double
)
