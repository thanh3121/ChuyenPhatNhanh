# Tài liệu Dự án BTL_Mobile - Lightning Transport

Hệ thống quản lý vận chuyển thông minh với 3 đối tượng sử dụng: Khách hàng, Nhân viên và Quản trị viên (Admin).

---

## 1. Phân loại các File theo Actor

### **A. Actor: Admin (Quản trị viên)**
Quản trị viên quản lý hệ thống thông qua giao diện Web được phục vụ bởi ứng dụng Android.
- **Logic & API**:
    - `AdminApi.kt`: Chứa logic xử lý API cho Admin (quản lý bảng giá, tài khoản, nhân viên, thống kê).
    - `AdminServer.kt`: Khởi tạo Ktor Server để phục vụ giao diện Web Admin.
    - `AdminService.kt`: Service chạy ngầm để duy trì Server Admin trên điện thoại.
- **Giao diện Web**:
    - `assets/admin_web/`: Chứa mã nguồn giao diện Web (HTML, JS, CSS).

### **B. Actor: Khách hàng (Customer)**
Người sử dụng dịch vụ để tạo và theo dõi đơn hàng trên ứng dụng di động.
- **Màn hình & Logic**:
    - `manHinhKH.kt`: Màn hình chính (Navigation điều hướng).
    - `TaoDonHang.kt`: Logic tạo đơn hàng, tính phí.
    - `LichSu_DonHang.kt`: Quản lý danh sách đơn hàng đã đặt.
    - `QuanLyDiaChi.kt`: Quản lý sổ địa chỉ.
    - `ThemDiaChiActivity.kt`: Thêm mới địa chỉ.
    - `HoSoCaNhan.kt`: Quản lý thông tin cá nhân.
- **Giao diện (XML Layout)**:
    - `activity_customer.xml`: Giao diện khung chính khách hàng.
    - `fragment_create_order.xml`: Giao diện form tạo đơn hàng.
    - `fragment_order_history.xml`: Giao diện danh sách lịch sử đơn.
    - `activity_address_management.xml`: Giao diện quản lý sổ địa chỉ.
    - `activity_add_address.xml`: Giao diện thêm địa chỉ mới.
    - `fragment_profile.xml`: Giao diện hồ sơ cá nhân khách hàng.

### **C. Actor: Nhân viên (Staff/Driver)**
Người tiếp nhận và cập nhật tiến độ giao hàng.
- **Màn hình & Logic**:
    - `manHinhNV.kt`: Màn hình chính của nhân viên.
    - `DanhSachDonHang.kt`: Quản lý danh sách đơn (Mới/Đã nhận).
    - `ChiTietDonDialog.kt`: Dialog xem chi tiết và cập nhật trạng thái.
    - `ThongKe_NV.kt`: Logic báo cáo năng suất.
    - `HoSo_NV.kt`: Quản lý hồ sơ nhân viên.
- **Giao diện (XML Layout)**:
    - `activity_employee.xml`: Giao diện khung chính nhân viên.
    - `fragment_order_list.xml`: Giao diện danh sách đơn hàng cho NV.
    - `dialog_order_detail.xml`: Giao diện chi tiết đơn hàng (Cập nhật trạng thái).
    - `fragment_employee_stats.xml`: Giao diện biểu đồ thống kê.
    - `fragment_employee_profile.xml`: Giao diện hồ sơ nhân viên.

### **D. Thành phần dùng chung (Common)**
- **Logic**:
    - `DangNhapActivity.kt` / `activity_main.xml`: Màn hình đăng nhập.
    - `DangKy.kt` / `activity_register.xml`: Đăng ký khách hàng.
    - `DoiMatKhau.kt` / `activity_change_password.xml`: Đổi mật khẩu.
    - `DbHelper.kt`: Quản lý SQLite.
    - `DonVanChuyen.kt`: Model dữ liệu.
    - `ChonViTri_Map.kt` / `activity_map_picker.xml`: Bản đồ Google Maps.
    - `DanhSachDonAdapter.kt` / `item_order.xml`: Hiển thị từng dòng đơn hàng.
- **Khác**:
    - `item_address.xml`: Layout cho từng địa chỉ trong danh sách.
    - `ChuyenDoiDiaChi.kt`: Utility tọa độ -> địa chỉ.

---

## 2. Mô tả Chức năng & Hàm xử lý chi tiết

### **Đối với Khách hàng**
1. **Tạo đơn hàng (`TaoDonHang.kt`)**:
   - `saveImageToInternalStorage()`: Lưu ảnh mặt hàng vào bộ nhớ cục bộ để đảm bảo an toàn và quyền truy cập.
   - `loadPickupAddresses()`: Tải danh sách địa chỉ lấy hàng vào Spinner.
   - `calculateDistanceAndPrice()`: Tự động tính quãng đường và phí vận chuyển dựa trên gói cước, khối lượng và vị trí.
   - `submitOrder()`: Kiểm tra dữ liệu và lưu đơn hàng mới vào cơ sở dữ liệu.
   - `ChonViTri_Map.kt`: Xử lý chọn tọa độ giao hàng trực quan trên bản đồ.

2. **Theo dõi đơn hàng (`LichSu_DonHang.kt`)**:
   - `loadOrders()`: Truy vấn danh sách đơn hàng của khách hàng theo `ma_khach_hang`.
   - `showOrderDetails()`: Hiển thị thông báo chi tiết trạng thái đơn hàng.
   - `cancelOrder()`: Cho phép khách hàng hủy đơn khi vẫn đang ở trạng thái chờ.

3. **Quản lý thông tin & Địa chỉ**:
   - `HoSoCaNhan.kt`: Cập nhật thông tin cá nhân khách hàng.
   - `QuanLyDiaChi.kt`: Hiển thị danh sách địa chỉ lấy hàng đã lưu.
   - `ThemDiaChiActivity.kt`: Thêm địa chỉ mới (phối hợp với `ChonViTri_Map.kt`).

### **Đối với Nhân viên**
1. **Tiếp nhận đơn (`DanhSachDonHang.kt`)**:
   - `loadOrders()`: Lọc danh sách đơn "Đang xử lý" (Đơn mới) hoặc đơn nhân viên đã nhận.
   - `showOrderDetail()`: Mở dialog để xem thông tin chi tiết và xử lý đơn.

2. **Giao hàng & Cập nhật (`ChiTietDonDialog.kt`)**:
   - `getPickupAddress()`: Lấy địa chỉ lấy hàng đầy đủ (Xã, Huyện, Tỉnh) từ ID địa chỉ nhận.
   - `acceptOrder()`: Gán mã nhân viên vào đơn hàng và chuyển trạng thái sang "Chờ lấy hàng".
   - `updateOrderStatus()`: Cập nhật tiến độ vận chuyển (Chờ giao hàng, Thành công, Thất bại).
   - `initViews()`: Hiển thị ảnh mặt hàng (`iv_dlg_image`) và các thông tin liên quan.

3. **Thống kê & Hồ sơ (`ThongKe_NV.kt`)**:
   - `loadStats()`: Tính toán tổng số đơn, đơn thành công và đơn thất bại để hiển thị báo cáo.
   - `HoSo_NV.kt`: Quản lý và cập nhật thông tin cá nhân nhân viên.

---
*Tài liệu được cập nhật bổ sung tên các hàm xử lý chính tương ứng với từng file Logic.*
