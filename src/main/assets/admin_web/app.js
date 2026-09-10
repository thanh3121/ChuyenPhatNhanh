// App State
let currentSection = 'dashboard';
let currentAccountType = 'khach_hang';
let charts = {};

// Initial Load
document.addEventListener('DOMContentLoaded', () => {
    showSection('dashboard');
    refreshStats();
});

// Navigation
function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('#content-area > section').forEach(s => s.classList.add('hidden'));
    // Show selected
    document.getElementById(`section-${sectionId}`).classList.remove('hidden');

    // Update Sidebar
    document.querySelectorAll('.sidebar-item').forEach(item => item.classList.remove('active'));
    document.getElementById(`nav-${sectionId}`).classList.add('active');

    // Update Title
    const titles = {
        'dashboard': 'Dashboard / Tổng quan',
        'prices': 'Quản lý bảng giá vận chuyển',
        'accounts': 'Quản lý tài khoản người dùng',
        'employees': 'Quản lý hồ sơ nhân viên',
        'orders': 'Quản lý ủy thác vận chuyển'
    };
    document.getElementById('section-title').innerText = titles[sectionId];

    currentSection = sectionId;
    loadSectionData(sectionId);
}

function loadSectionData(sectionId) {
    switch(sectionId) {
        case 'dashboard': refreshStats(); break;
        case 'prices': loadPrices(); break;
        case 'accounts': loadAccounts(currentAccountType); break;
        case 'employees': loadEmployees(); break;
        case 'orders': loadOrders(); break;
    }
}

// --- Dashboard & Stats ---
async function refreshStats() {
    try {
        const response = await fetch('/api/stats');
        const stats = await response.json();

        document.getElementById('stat-total-orders').innerText = stats.totalOrders;
        document.getElementById('stat-total-revenue').innerText = stats.totalRevenue.toLocaleString() + 'đ';

        updateChart('chartStatus', 'pie', stats.ordersByStatus);
        updateChart('chartRoute', 'bar', stats.revenueByRoute);
    } catch (e) { console.error('Error fetching stats', e); }
}

function updateChart(chartId, type, data) {
    if (charts[chartId]) charts[chartId].destroy();

    const ctx = document.getElementById(chartId).getContext('2d');
    charts[chartId] = new Chart(ctx, {
        type: type,
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                data: data.map(d => d.value),
                backgroundColor: ['#f97316', '#ef4444', '#dc2626', '#fbbf24', '#f87171']
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'bottom' } }
        }
    });
}

// --- Prices ---
async function loadPrices() {
    try {
        const res = await fetch('/api/rates');
        const rates = await res.json();
        const tbody = document.getElementById('prices-table-body');
        tbody.innerHTML = rates.map(r => `
            <tr class="border-b hover:bg-gray-50 transition">
                <td class="px-6 py-4">${r.ten_tuyen || 'N/A'}</td>
                <td class="px-6 py-4 font-medium">${(r.cuoc_co_ban || 0).toLocaleString()}đ</td>
                <td class="px-6 py-4">+${(r.don_gia_vuot_km || 0).toLocaleString()}đ</td>
                <td class="px-6 py-4">${r.khoi_luong_co_ban || 0}kg (tiếp: +${(r.don_gia_vuot_khoi_luong || 0).toLocaleString()}đ)</td>
                <td class="px-6 py-4">${(r.phi_dong_goi || 0).toLocaleString()}đ</td>
                <td class="px-6 py-4">
                    <button onclick='editRate(${JSON.stringify(r)})' class="text-orange-600 hover:text-orange-800 font-medium">Sửa</button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading prices:', e);
        document.getElementById('prices-table-body').innerHTML = '<tr><td colspan="6" class="p-4 text-center text-red-500">Lỗi tải dữ liệu bảng giá</td></tr>';
    }
}

function editRate(rate) {
    const html = `
        <div class="bg-white p-8 rounded-2xl shadow-2xl w-full max-w-md animate-fade-in">
            <h3 class="text-2xl font-bold mb-6 text-orange-600">Sửa bảng giá: ${rate.ten_tuyen}</h3>
            <form id="rate-form" class="space-y-4">
                <input type="hidden" name="ma_goi_cuoc" value="${rate.ma_goi_cuoc}">
                <input type="hidden" name="ten_tuyen" value="${rate.ten_tuyen}">
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Cước cơ bản (VNĐ)</label>
                        <input type="number" name="cuoc_co_ban" value="${rate.cuoc_co_ban}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm focus:ring-orange-500 border-gray-300">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Đơn giá vượt KM</label>
                        <input type="number" name="don_gia_vuot_km" value="${rate.don_gia_vuot_km}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm focus:ring-orange-500 border-gray-300">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Khối lượng cơ bản (kg)</label>
                        <input type="number" step="0.1" name="khoi_luong_co_ban" value="${rate.khoi_luong_co_ban}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Đơn giá vượt KL</label>
                        <input type="number" name="don_gia_vuot_khoi_luong" value="${rate.don_gia_vuot_khoi_luong}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Phí bảo hiểm</label>
                        <input type="number" name="phi_bao_hiem" value="${rate.phi_bao_hiem}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Phí đóng gói</label>
                        <input type="number" name="phi_dong_goi" value="${rate.phi_dong_goi}" class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                    </div>
                </div>
                <div class="flex justify-end space-x-3 mt-8">
                    <button type="button" onclick="closeModal()" class="px-4 py-2 text-gray-500 hover:text-gray-700">Hủy</button>
                    <button type="submit" class="bg-orange-600 text-white px-6 py-2 rounded-lg font-bold hover:bg-orange-700 shadow-lg">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    `;
    showModal(html);
    document.getElementById('rate-form').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData.entries());
        // Convert numbers
        ['cuoc_co_ban', 'don_gia_vuot_km', 'phi_bao_hiem', 'phi_dong_goi', 'khoi_luong_co_ban', 'don_gia_vuot_khoi_luong'].forEach(k => {
            if (data[k]) data[k] = parseFloat(data[k]);
        });
        data.ma_goi_cuoc = parseInt(data.ma_goi_cuoc);

        const res = await fetch('/api/rates/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            loadPrices();
        }
    };
}

// --- Accounts ---
async function loadAccounts(type) {
    currentAccountType = type;
    // Update tabs UI
    document.getElementById('btn-acc-customer').className = type === 'khach_hang' ? 'px-4 py-2 rounded-lg bg-orange-600 text-white shadow-md' : 'px-4 py-2 rounded-lg bg-white border border-gray-200 hover:bg-gray-50';
    document.getElementById('btn-acc-employee').className = type === 'nhan_vien' ? 'px-4 py-2 rounded-lg bg-orange-600 text-white shadow-md' : 'px-4 py-2 rounded-lg bg-white border border-gray-200 hover:bg-gray-50';

    // Show/Hide Add Account button for employees
    const addBtnHtml = type === 'nhan_vien' ? `<button onclick="openAccountModal()" class="ml-auto bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition shadow-sm"><i class="fas fa-plus mr-2"></i>Thêm tài khoản NV</button>` : '';
    document.querySelector('#section-accounts .flex').innerHTML = `
        <button onclick="loadAccounts('khach_hang')" id="btn-acc-customer" class="${type === 'khach_hang' ? 'px-4 py-2 rounded-lg bg-orange-600 text-white shadow-md' : 'px-4 py-2 rounded-lg bg-white border border-gray-200 hover:bg-gray-50'}">Khách hàng</button>
        <button onclick="loadAccounts('nhan_vien')" id="btn-acc-employee" class="${type === 'nhan_vien' ? 'px-4 py-2 rounded-lg bg-orange-600 text-white shadow-md' : 'px-4 py-2 rounded-lg bg-white border border-gray-200 hover:bg-gray-50'}">Nhân viên</button>
        ${addBtnHtml}
    `;

    const res = await fetch(`/api/accounts/${type}`);
    const accounts = await res.json();

    const header = document.getElementById('accounts-header');
    header.innerHTML = type === 'khach_hang'
        ? '<th class="px-6 py-4">Tài khoản</th><th class="px-6 py-4">Họ tên</th><th class="px-6 py-4">Số điện thoại</th><th class="px-6 py-4">Trạng thái</th><th class="px-6 py-4">Thao tác</th>'
        : '<th class="px-6 py-4">Tài khoản</th><th class="px-6 py-4">Mã NV</th><th class="px-6 py-4">Họ tên</th><th class="px-6 py-4">Trạng thái</th><th class="px-6 py-4">Thao tác</th>';

    const tbody = document.getElementById('accounts-table-body');
    tbody.innerHTML = accounts.map(acc => `
        <tr class="border-b hover:bg-gray-50 transition">
            <td class="px-6 py-4">${acc.tai_khoan}</td>
            ${type === 'khach_hang' ? `
                <td class="px-6 py-4">${acc.ho_ten || 'N/A'}</td>
                <td class="px-6 py-4">${acc.so_dien_thoai || 'N/A'}</td>
            ` : `
                <td class="px-6 py-4">${acc.ma_nhan_vien || 'N/A'}</td>
                <td class="px-6 py-4">${acc.ho_ten || 'N/A'}</td>
            `}
            <td class="px-6 py-4">
                <span class="px-2 py-1 rounded-full text-xs font-bold ${acc.trang_thai.toLowerCase().includes('hoat') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
                    ${acc.trang_thai.toLowerCase().includes('hoat') ? 'Hoạt động' : 'Bị chặn'}
                </span>
            </td>
            <td class="px-6 py-4">
                ${type === 'khach_hang' ? `
                    <button onclick="toggleAccount(${acc.ma_tai_khoan}, '${acc.trang_thai}')" class="${acc.trang_thai.toLowerCase().includes('hoat') ? 'text-red-600' : 'text-green-600'} font-medium mr-3">
                        ${acc.trang_thai.toLowerCase().includes('hoat') ? 'Chặn' : 'Bỏ chặn'}
                    </button>
                ` : `
                    <button onclick='editAccount(${JSON.stringify(acc)})' class="text-orange-600 font-medium mr-3">Sửa</button>
                `}
            </td>
        </tr>
    `).join('');
}

function editAccount(acc) {
    const html = `
        <div class="bg-white p-8 rounded-2xl shadow-2xl w-full max-w-md animate-fade-in">
            <h3 class="text-2xl font-bold mb-6 text-orange-600">Sửa tài khoản: ${acc.tai_khoan}</h3>
            <form id="edit-acc-form" class="space-y-4">
                <input type="hidden" name="ma_tai_khoan" value="${acc.ma_tai_khoan}">
                <div>
                    <label class="block text-sm font-medium text-gray-700">Tài khoản (Username)</label>
                    <input type="text" name="tai_khoan" value="${acc.tai_khoan}" required class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Mật khẩu mới (Để trống nếu không đổi)</label>
                    <input type="password" name="mat_khau" placeholder="********" class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Mã nhân viên áp dụng</label>
                    <input type="text" name="ma_nhan_vien" value="${acc.ma_nhan_vien || ''}" required class="mt-1 block w-full border rounded-lg p-2 shadow-sm border-gray-300">
                    <p class="text-xs text-gray-500 mt-1">Thay đổi mã NV để chuyển tài khoản cho người khác.</p>
                </div>
                <div class="flex justify-end space-x-3 mt-8">
                    <button type="button" onclick="closeModal()" class="px-4 py-2 text-gray-500 hover:text-gray-700">Hủy</button>
                    <button type="submit" class="bg-orange-600 text-white px-6 py-2 rounded-lg font-bold hover:bg-orange-700 shadow-lg">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    `;
    showModal(html);
    document.getElementById('edit-acc-form').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData.entries());
        data.ma_tai_khoan = parseInt(data.ma_tai_khoan);

        const res = await fetch('/api/accounts/employee/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            loadAccounts('nhan_vien');
        } else {
            alert('Lỗi: Kiểm tra lại mã nhân viên hoặc tài khoản.');
        }
    };
}

function openAccountModal() {
    const html = `
        <div class="bg-white p-8 rounded-2xl shadow-2xl w-full max-w-md animate-fade-in">
            <h3 class="text-2xl font-bold mb-6 text-orange-600">Thêm tài khoản nhân viên</h3>
            <form id="acc-form" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700">Tài khoản (Username)</label>
                    <input type="text" name="tai_khoan" required class="mt-1 block w-full border rounded-lg p-2 shadow-sm">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Mật khẩu</label>
                    <input type="password" name="mat_khau" required class="mt-1 block w-full border rounded-lg p-2 shadow-sm">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Mã nhân viên áp dụng</label>
                    <input type="text" name="ma_nhan_vien" required placeholder="Nhập đúng mã NV đã tạo" class="mt-1 block w-full border rounded-lg p-2 shadow-sm">
                </div>
                <div class="flex justify-end space-x-3 mt-8">
                    <button type="button" onclick="closeModal()" class="px-4 py-2 text-gray-500 hover:text-gray-700">Hủy</button>
                    <button type="submit" class="bg-green-600 text-white px-6 py-2 rounded-lg font-bold hover:bg-green-700 shadow-lg">Tạo tài khoản</button>
                </div>
            </form>
        </div>
    `;
    showModal(html);
    document.getElementById('acc-form').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData.entries());

        const res = await fetch('/api/accounts/employee/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            loadAccounts('nhan_vien');
        } else {
            alert('Lỗi: Có thể mã nhân viên không tồn tại hoặc tài khoản đã có.');
        }
    };
}

async function toggleAccount(id, status) {
    if (!confirm('Bạn có chắc muốn thay đổi trạng thái tài khoản này?')) return;
    await fetch('/api/accounts/toggle', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id, status })
    });
    loadSectionData('accounts');
}

// --- Employees ---
async function loadEmployees() {
    const res = await fetch('/api/employees');
    const emps = await res.json();
    const tbody = document.getElementById('employees-table-body');
    tbody.innerHTML = emps.map(e => `
        <tr class="border-b hover:bg-gray-50">
            <td class="px-6 py-4 font-bold">${e.ma_nhan_vien}</td>
            <td class="px-6 py-4">${e.ho_ten}</td>
            <td class="px-6 py-4">${e.so_dien_thoai}</td>
            <td class="px-6 py-4 text-sm text-gray-500">${e.ngay_vao_lam}</td>
            <td class="px-6 py-4">
                <span class="px-2 py-1 rounded-full text-xs ${e.trang_thai === 'Dang hoat dong' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}">
                    ${e.trang_thai}
                </span>
            </td>
            <td class="px-6 py-4">
                <button onclick='openEmployeeModal(${JSON.stringify(e)})' class="text-orange-600 mr-3">Sửa</button>
            </td>
        </tr>
    `).join('');
}

function openEmployeeModal(emp = null) {
    const isEdit = !!emp;
    const html = `
        <div class="bg-white p-8 rounded-2xl shadow-2xl w-full max-w-lg animate-fade-in">
            <h3 class="text-2xl font-bold mb-6 text-orange-600">${isEdit ? 'Sửa hồ sơ nhân viên' : 'Thêm nhân viên mới'}</h3>
            <form id="emp-form" class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Mã nhân viên</label>
                        <input type="text" name="ma_nhan_vien" value="${emp?.ma_nhan_vien || ''}" ${isEdit ? 'readonly class="mt-1 block w-full border rounded-lg p-2 bg-gray-100"' : 'class="mt-1 block w-full border rounded-lg p-2"'}>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Họ và tên</label>
                        <input type="text" name="ho_ten" value="${emp?.ho_ten || ''}" required class="mt-1 block w-full border rounded-lg p-2">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Số điện thoại</label>
                        <input type="text" name="so_dien_thoai" value="${emp?.so_dien_thoai || ''}" class="mt-1 block w-full border rounded-lg p-2">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">CCCD</label>
                        <input type="text" name="cccd" value="${emp?.cccd || ''}" class="mt-1 block w-full border rounded-lg p-2">
                    </div>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Quê quán</label>
                    <input type="text" name="que_quan" value="${emp?.que_quan || ''}" class="mt-1 block w-full border rounded-lg p-2">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">Trạng thái</label>
                    <select name="trang_thai" class="mt-1 block w-full border rounded-lg p-2">
                        <option value="Dang hoat dong" ${emp?.trang_thai === 'Dang hoat dong' ? 'selected' : ''}>Đang hoạt động</option>
                        <option value="Dung lam viec" ${emp?.trang_thai === 'Dung lam viec' ? 'selected' : ''}>Dừng làm việc</option>
                    </select>
                </div>
                <div class="flex justify-end space-x-3 mt-8">
                    <button type="button" onclick="closeModal()" class="px-4 py-2 text-gray-500 hover:text-gray-700">Hủy</button>
                    <button type="submit" class="bg-orange-600 text-white px-6 py-2 rounded-lg font-bold hover:bg-orange-700">Lưu hồ sơ</button>
                </div>
            </form>
        </div>
    `;
    showModal(html);
    document.getElementById('emp-form').onsubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData.entries());

        const res = await fetch('/api/employees/upsert', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            loadEmployees();
        }
    };
}

// --- Utils ---
function showModal(contentHtml) {
    const container = document.getElementById('modal-container');
    container.innerHTML = contentHtml;
    container.classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modal-container').classList.add('hidden');
}

// --- Orders ---
async function loadOrders() {
    try {
        const res = await fetch('/api/orders');
        const orders = await res.json();
        const tbody = document.getElementById('orders-table-body');
        tbody.innerHTML = orders.map(o => `
            <tr class="border-b hover:bg-gray-50 cursor-pointer" onclick="showOrderDetails(${JSON.stringify(o).replace(/"/g, '&quot;')})">
                <td class="px-6 py-4 font-mono text-xs font-bold text-black">#${o.ma_don}</td>
                <td class="px-6 py-4">
                    <div class="text-sm font-bold">${o.ten_khach_hang || 'N/A'}</div>
                    <div class="text-xs text-gray-500">${o.so_dien_thoai || ''}</div>
                </td>
                <td class="px-6 py-4 text-sm">${o.ten_goi_cuoc || 'N/A'}</td>
                <td class="px-6 py-4 font-bold text-orange-600">${(o.tong_thanh_toan || 0).toLocaleString()}đ</td>
                <td class="px-6 py-4 text-sm">${o.ma_nhan_vien_giao || '<i>Chưa có</i>'}</td>
                <td class="px-6 py-4">
                    <span class="px-2 py-1 rounded-full text-xs font-bold bg-orange-100 text-orange-700">${o.trang_thai}</span>
                </td>
                <td class="px-6 py-4" onclick="event.stopPropagation()">
                    ${o.trang_thai.toLowerCase().includes('xu ly') ? `
                        <button onclick="updateOrder(${o.ma_don}, 'Cho lay hang')" class="bg-green-500 text-white px-4 py-1.5 rounded-lg text-xs font-bold hover:bg-green-600 shadow-sm transition">Xác nhận</button>
                        <button onclick="rejectOrder(${o.ma_don})" class="bg-red-500 text-white px-4 py-1.5 rounded-lg text-xs font-bold ml-1 hover:bg-red-600 shadow-sm transition">Từ chối</button>
                    ` : `<span class="text-gray-400 text-xs">N/A</span>`}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Error loading orders:', e);
        document.getElementById('orders-table-body').innerHTML = '<tr><td colspan="7" class="p-4 text-center text-red-500">Lỗi tải dữ liệu đơn hàng</td></tr>';
    }
}

function showOrderDetails(o) {
    const html = `
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl overflow-hidden animate-fade-in">
            <div class="bg-gray-100 p-6 border-b flex justify-between items-center">
                <h3 class="text-xl font-bold text-black">Mã đơn: #${o.ma_don}</h3>
                <button onclick="closeModal()" class="text-gray-500 hover:text-black"><i class="fas fa-times text-xl"></i></button>
            </div>
            <div class="p-8 grid grid-cols-2 gap-8 overflow-y-auto max-h-[80vh]">
                <div class="space-y-4">
                    <h4 class="font-bold border-b pb-2 text-gray-400 uppercase text-xs">Người gửi & Mặt hàng</h4>
                    <p><span class="text-gray-500">Khách hàng:</span> <br><b>${o.ten_khach_hang}</b> (${o.so_dien_thoai})</p>
                    <p><span class="text-gray-500">Địa chỉ lấy:</span> <br><b class="text-orange-600">${o.dia_chi_lay || 'N/A'}</b></p>
                    ${o.anh_mat_hang ? `
                        <div class="mt-2">
                            <p class="text-gray-500 text-[10px] uppercase font-bold mb-1">Ảnh mặt hàng:</p>
                            <img src="/api/image?uri=${encodeURIComponent(o.anh_mat_hang)}" class="w-full h-40 object-cover rounded-xl border-2 border-orange-100 shadow-sm" onerror="this.parentElement.innerHTML='<div class=\'p-4 bg-gray-100 rounded-lg text-xs text-gray-400\'>Lỗi tải ảnh hoặc ảnh không tồn tại</div>'">
                        </div>
                    ` : ''}
                    <p><span class="text-gray-500">Mô tả:</span> <br>${o.mo_ta || 'Không có mô tả'}</p>
                    <p><span class="text-gray-500">Khối lượng:</span> ${o.khoi_luong}kg</p>
                    <div class="bg-gray-50 p-3 rounded-lg border italic text-sm text-gray-600">
                        Ghi chú: ${o.ghi_chu || 'Không có'}
                    </div>
                </div>
                <div class="space-y-4">
                    <h4 class="font-bold border-b pb-2 text-gray-400 uppercase text-xs">Người nhận & Giao hàng</h4>
                    <p><span class="text-gray-500">Người nhận:</span> <br><b>${o.ten_nguoi_nhan}</b> (${o.sdt_nguoi_nhan})</p>
                    <p><span class="text-gray-500">Địa chỉ giao:</span> <br><b class="text-blue-600">${o.dia_chi_giao}</b></p>
                    <p><span class="text-gray-500">Gói cước:</span> <span class="text-orange-600 font-bold">${o.ten_goi_cuoc}</span></p>
                    <div class="space-y-1 text-sm">
                        <div class="flex justify-between"><span>COD:</span> <b>${o.co_cod ? 'Có' : 'Không'}</b></div>
                        <div class="flex justify-between"><span>Bảo hiểm:</span> <b>${o.co_bao_hiem ? 'Có' : 'Không'}</b></div>
                        <div class="flex justify-between"><span>Đóng gói:</span> <b>${o.co_dong_goi_dac_biet ? 'Có' : 'Không'}</b></div>
                        <div class="flex justify-between text-lg border-t pt-2 mt-2 font-bold text-red-600">
                            <span>TỔNG:</span> <span>${o.tong_thanh_toan.toLocaleString()}đ</span>
                        </div>
                    </div>
                </div>
                ${o.li_do ? `
                    <div class="col-span-2 bg-red-50 p-4 rounded-xl border border-red-100">
                        <p class="text-red-700 font-bold text-sm uppercase">Lý do thất bại/từ chối:</p>
                        <p class="text-red-600">${o.li_do}</p>
                    </div>
                ` : ''}
            </div>
            <div class="p-6 bg-gray-50 flex justify-end">
                <button onclick="closeModal()" class="btn-primary px-8 py-2 rounded-xl font-bold">Đóng</button>
            </div>
        </div>
    `;
    showModal(html);
}

async function updateOrder(id, status, reason = null) {
    await fetch('/api/orders/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id, status, reason })
    });
    loadOrders();
}

function rejectOrder(id) {
    const reason = prompt('Lý do từ chối:');
    if (reason) updateOrder(id, 'Tu choi', reason);
}
