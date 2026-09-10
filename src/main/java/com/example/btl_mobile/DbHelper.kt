package com.example.btl_mobile

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "chuyen_phat_nhanh.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Bật hỗ trợ khóa ngoại
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. BẢNG GÓI CƯỚC
        db.execSQL("""
            CREATE TABLE goi_cuoc (
                ma_goi_cuoc     INTEGER PRIMARY KEY AUTOINCREMENT,
                ten_tuyen       TEXT NOT NULL CHECK (ten_tuyen IN ('Noi tinh', 'Noi mien', 'Lien mien')),
                cuoc_co_ban     REAL,
                km_mien_phi     REAL,
                don_gia_vuot_km REAL,
                khoi_luong_co_ban REAL,
                buoc_khoi_luong   REAL,
                don_gia_vuot_khoi_luong REAL,
                phi_cod         REAL DEFAULT 0,
                phi_bao_hiem    REAL,
                phi_dong_goi    REAL,
                tg_giao_toi_thieu INTEGER,
                tg_giao_toi_da    INTEGER,
                ghi_chu         TEXT
            );
        """)

        // 2. BẢNG TÀI KHOẢN
        db.execSQL("""
            CREATE TABLE tai_khoan (
                ma_tai_khoan    INTEGER PRIMARY KEY AUTOINCREMENT,
                tai_khoan       TEXT NOT NULL UNIQUE,
                mat_khau        TEXT NOT NULL,
                loai_tai_khoan  TEXT NOT NULL CHECK (loai_tai_khoan IN ('khach_hang', 'nhan_vien', 'quan_tri_vien')),
                google_id       TEXT UNIQUE,
                trang_thai      TEXT NOT NULL DEFAULT 'Hoat dong' CHECK (trang_thai IN ('Hoat dong', 'Bi chan')),
                ngay_tao        TEXT DEFAULT (datetime('now'))
            );
        """)

        // 3. BẢNG KHÁCH HÀNG
        db.execSQL("""
            CREATE TABLE khach_hang (
                ma_khach_hang   INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_tai_khoan    INTEGER NOT NULL UNIQUE,
                ho_ten          TEXT NOT NULL,
                so_dien_thoai   TEXT NOT NULL UNIQUE,
                dia_chi         TEXT,
                FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan (ma_tai_khoan) ON DELETE CASCADE
            );
        """)

        // 4. BẢNG ĐỊA CHỈ NHẬN HÀNG
        db.execSQL("""
            CREATE TABLE dia_chi_nhan (
                ma_dia_chi      INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_khach_hang   INTEGER NOT NULL,
                mien            TEXT,
                tinh_thanh      TEXT,
                xa_phuong       TEXT,
                dia_chi_chi_tiet TEXT,
                vi_do           REAL,
                kinh_do         REAL,
                la_mac_dinh     INTEGER DEFAULT 0,
                FOREIGN KEY (ma_khach_hang) REFERENCES khach_hang (ma_khach_hang) ON DELETE CASCADE
            );
        """)

        // 5. BẢNG HỒ SƠ NHÂN VIÊN
        db.execSQL("""
            CREATE TABLE nhan_vien (
                ma_nhan_vien    TEXT PRIMARY KEY,
                ma_tai_khoan    INTEGER UNIQUE,
                ho_ten          TEXT NOT NULL,
                que_quan        TEXT,
                so_dien_thoai   TEXT,
                cccd            TEXT,
                ngay_vao_lam    TEXT DEFAULT (date('now')),
                trang_thai      TEXT NOT NULL DEFAULT 'Dang hoat dong' CHECK (trang_thai IN ('Dang hoat dong', 'Dung lam viec')),
                FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan (ma_tai_khoan) ON DELETE SET NULL
            );
        """)

        // 6. BẢNG ĐƠN VẬN CHUYỂN
        db.execSQL("""
            CREATE TABLE don_van_chuyen (
                ma_don              INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_khach_hang       INTEGER NOT NULL,
                ten_khach_hang      TEXT,
                so_dien_thoai       TEXT,
                ma_goi_cuoc         INTEGER NOT NULL,
                ma_dia_chi_nhan     INTEGER,
                dia_chi_giao        TEXT,
                vi_do_giao          REAL,
                kinh_do_giao        REAL,
                ten_nguoi_nhan      TEXT NOT NULL,
                sdt_nguoi_nhan      TEXT NOT NULL,
                anh_mat_hang        TEXT,
                mo_ta               TEXT,
                khoi_luong          REAL,
                quang_duong_km      REAL,
                co_cod              INTEGER DEFAULT 0,
                co_bao_hiem         INTEGER DEFAULT 0,
                co_dong_goi_dac_biet INTEGER DEFAULT 0,
                cuoc_van_chuyen     REAL,
                phi_bao_hiem        REAL DEFAULT 0,
                phi_dong_goi        REAL DEFAULT 0,
                tong_thanh_toan     REAL,
                ghi_chu             TEXT,
                ma_nhan_vien_giao   TEXT,
                trang_thai          TEXT NOT NULL DEFAULT 'Dang xu ly' CHECK (trang_thai IN ('Dang xu ly', 'Cho lay hang', 'Cho giao hang', 'Hoan tat', 'Thanh cong', 'That bai', 'Tu choi', 'Da huy')),
                li_do               TEXT,
                ngay_tao            TEXT DEFAULT (datetime('now')),
                ngay_cap_nhat       TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (ma_khach_hang) REFERENCES khach_hang (ma_khach_hang) ON DELETE CASCADE,
                FOREIGN KEY (ma_goi_cuoc) REFERENCES goi_cuoc (ma_goi_cuoc),
                FOREIGN KEY (ma_dia_chi_nhan) REFERENCES dia_chi_nhan (ma_dia_chi),
                FOREIGN KEY (ma_nhan_vien_giao) REFERENCES nhan_vien (ma_nhan_vien) ON DELETE SET NULL
            );
        """)

        // 7. BẢNG ĐÁNH GIÁ / PHẢN HỒI
        db.execSQL("""
            CREATE TABLE danh_gia (
                ma_danh_gia     INTEGER PRIMARY KEY AUTOINCREMENT,
                ma_don          INTEGER NOT NULL UNIQUE,
                ma_khach_hang   INTEGER NOT NULL,
                ma_nhan_vien    TEXT,
                so_sao          INTEGER CHECK (so_sao BETWEEN 1 AND 5),
                binh_luan       TEXT,
                ngay_danh_gia   TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (ma_don) REFERENCES don_van_chuyen (ma_don) ON DELETE CASCADE,
                FOREIGN KEY (ma_khach_hang) REFERENCES khach_hang (ma_khach_hang) ON DELETE CASCADE,
                FOREIGN KEY (ma_nhan_vien) REFERENCES nhan_vien (ma_nhan_vien) ON DELETE SET NULL
            );
        """)

        // INDEX HỖ TRỢ TRUY VẤN
        db.execSQL("CREATE INDEX idx_don_trang_thai ON don_van_chuyen (trang_thai);")
        db.execSQL("CREATE INDEX idx_don_nhan_vien ON don_van_chuyen (ma_nhan_vien_giao);")
        db.execSQL("CREATE INDEX idx_don_khach_hang ON don_van_chuyen (ma_khach_hang);")
        db.execSQL("CREATE INDEX idx_don_ngay_tao ON don_van_chuyen (ngay_tao);")
        db.execSQL("CREATE INDEX idx_don_goi_cuoc ON don_van_chuyen (ma_goi_cuoc);")
        db.execSQL("CREATE INDEX idx_dia_chi_khach_hang ON dia_chi_nhan (ma_khach_hang);")

        // DỮ LIỆU MẪU
        db.execSQL("""
            INSERT INTO goi_cuoc (ten_tuyen, cuoc_co_ban, km_mien_phi, don_gia_vuot_km, khoi_luong_co_ban, buoc_khoi_luong, don_gia_vuot_khoi_luong, phi_cod, phi_bao_hiem, phi_dong_goi, tg_giao_toi_thieu, tg_giao_toi_da, ghi_chu)
            VALUES ('Noi tinh', 15000, NULL, NULL, 1, 0.5, 2500, 0, 9900, 5000, 1, 1, 'Cuoc co dinh 15.000d/don');
        """)
        db.execSQL("""
            INSERT INTO goi_cuoc (ten_tuyen, cuoc_co_ban, km_mien_phi, don_gia_vuot_km, khoi_luong_co_ban, buoc_khoi_luong, don_gia_vuot_khoi_luong, phi_cod, phi_bao_hiem, phi_dong_goi, tg_giao_toi_thieu, tg_giao_toi_da, ghi_chu)
            VALUES ('Noi mien', 15000, 5, 3000, 1, 0.5, 2500, 0, 9900, 5000, 1, 5, 'Duoi 5km: 15.000d; vuot 5km: +3.000d/km');
        """)
        db.execSQL("""
            INSERT INTO goi_cuoc (ten_tuyen, cuoc_co_ban, km_mien_phi, don_gia_vuot_km, khoi_luong_co_ban, buoc_khoi_luong, don_gia_vuot_khoi_luong, phi_cod, phi_bao_hiem, phi_dong_goi, tg_giao_toi_thieu, tg_giao_toi_da, ghi_chu)
            VALUES ('Lien mien', 15000, 5, 3000, 1, 0.5, 2500, 0, 9900, 5000, 3, 7, 'Duoi 5km: 15.000d; vuot 5km: +3.000d/km');
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS danh_gia")
        db.execSQL("DROP TABLE IF EXISTS don_van_chuyen")
        db.execSQL("DROP TABLE IF EXISTS nhan_vien")
        db.execSQL("DROP TABLE IF EXISTS dia_chi_nhan")
        db.execSQL("DROP TABLE IF EXISTS khach_hang")
        db.execSQL("DROP TABLE IF EXISTS tai_khoan")
        db.execSQL("DROP TABLE IF EXISTS goi_cuoc")
        onCreate(db)
    }
}
