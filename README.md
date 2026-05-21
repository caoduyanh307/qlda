# Traffic JavaFX MVC

Dự án JavaFX quản lý và phân tích lưu lượng giao thông TP.HCM, đã được tổ chức lại theo mô hình MVC.

## Cấu trúc chính

```text
src/main/java/com/ddangkhoa/trafficfx
├── MainApp.java
├── controller
│   ├── AreaManagementController.java
│   ├── DashboardController.java
│   ├── GenericManagementController.java
│   └── LoginController.java
├── dao
│   ├── AreaDao.java
│   └── GenericCrudDao.java
├── database
│   └── DatabaseManager.java
├── model
│   ├── AppUser.java
│   ├── Area.java
│   ├── DataRecord.java
│   ├── DemoRecord.java
│   └── Role.java
├── service
│   └── AuthService.java
└── view
    ├── DashboardView.java
    ├── LoginView.java
    ├── ManagementPage.java
    ├── RoleFeatures.java
    └── management
        ├── AreaManagementView.java
        ├── GenericManagementView.java
        └── ManagementViewFactory.java
```

## Ý nghĩa MVC

- `model`: class dữ liệu như `Area`, `AppUser`, `DataRecord`.
- `view`: giao diện JavaFX, chỉ dựng màn hình và nhận thao tác người dùng.
- `controller`: xử lý sự kiện, điều hướng màn hình và gọi DAO/service.
- `dao`: thao tác trực tiếp với SQLite database.
- `database`: tạo database, bảng và dữ liệu mẫu.
- `service`: xử lý nghiệp vụ dùng chung, ví dụ đăng nhập.

## Tài khoản mẫu

```text
admin / admin123
tech / tech123
analyst / analyst123
```

## Chạy chương trình

```bash
mvn clean javafx:run
```

Database SQLite tự tạo tại:

```text
data/traffic_management.db
```

## Cập nhật giao diện

Bản này giữ màn hình đăng nhập và nền giao diện cũ, bổ sung:

- Đồng bộ giao diện sidebar cho ADMIN, TECHNICIAN và ANALYST.
- Thêm mục **Thông tin tài khoản** ngay phía trên nút Đăng xuất.
- Thêm nút dấu ba chấm `⋯` trên từng module quản lý để Import/Export Excel `.xlsx` hoặc CSV `.csv`.
- Import nhận header theo tên cột hiển thị hoặc tên cột database.
- Export hỗ trợ xuất dữ liệu hiện có ra `.xlsx` hoặc `.csv`.
- Bổ sung highlight màu cho các cột quan trọng như vai trò, trạng thái, mức lưu lượng, vận tốc, kết quả phân tích.

