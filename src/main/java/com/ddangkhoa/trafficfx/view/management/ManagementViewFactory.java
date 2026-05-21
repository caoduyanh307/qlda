package com.ddangkhoa.trafficfx.view.management;

import com.ddangkhoa.trafficfx.model.AppUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ManagementViewFactory {
    private ManagementViewFactory() {}

    public static GenericManagementView accountView(AppUser user) {
        return view("Quản lý tài khoản", "Tìm kiếm tài khoản...", "Tổng tài khoản", "Danh Sách Tài Khoản",
                "account_management", "account_id", "AC", 3,
                List.of("account_id", "full_name", "email", "username", "password", "role_name", "assigned_at", "role_active", "account_status", "created_at", "updated_at", "is_delete"),
                mapOf("account_id", "Mã Tài Khoản", "full_name", "Họ Tên", "email", "Email", "username", "Tên Đăng Nhập",
                        "password", "Mật Khẩu", "role_name", "Vai Trò", "assigned_at", "Ngày Gán Quyền", "role_active", "Hiệu Lực Quyền",
                        "account_status", "Trạng Thái TK", "created_at", "Ngày Tạo", "updated_at", "Ngày Cập Nhật", "is_delete", "Đã Xóa"),
                mapOf("full_name", "Người dùng mới", "email", "newuser@uit.edu.vn", "username", "newuser", "password", "123456",
                        "role_name", "TECHNICIAN", "assigned_at", "2026-05-20", "role_active", "1", "account_status", "ACTIVE",
                        "created_at", "2026-05-20", "updated_at", "2026-05-20", "is_delete", "0"),
                true, user);
    }

    public static GenericManagementView accountRoleView(AppUser user) {
        return view("Quản lý phân quyền", "Tìm kiếm phân quyền...", "Tổng phân quyền", "Danh Sách Phân Quyền Tài Khoản",
                "account_role", "account_role_id", "AR", 3,
                List.of("account_role_id", "account_id", "role_name", "assigned_at", "is_active", "is_delete"),
                mapOf("account_role_id", "Mã Phân Quyền", "account_id", "Mã Tài Khoản", "role_name", "Vai Trò", "assigned_at", "Ngày Gán Quyền", "is_active", "Hiệu Lực", "is_delete", "Đã Xóa"),
                mapOf("account_id", "AC002", "role_name", "TECHNICIAN", "assigned_at", "2026-05-20", "is_active", "1", "is_delete", "0"),
                true, user);
    }

    public static GenericManagementView employeeView(AppUser user) {
        return view("Quản lý nhân viên", "Tìm kiếm nhân viên...", "Tổng nhân viên", "Danh Sách Nhân Viên",
                "employee", "employee_id", "NV", 3,
                List.of("employee_id", "fullname", "phonenumber", "email", "dateofbirth", "gender", "address", "salary", "status", "is_deleted"),
                mapOf("employee_id", "Mã Nhân Viên", "fullname", "Họ Tên", "phonenumber", "Số Điện Thoại", "email", "Email",
                        "dateofbirth", "Ngày Sinh", "gender", "Giới Tính", "address", "Địa Chỉ", "salary", "Lương", "status", "Trạng Thái", "is_deleted", "Đã Xóa"),
                mapOf("fullname", "Nhân viên mới", "phonenumber", "0900000000", "email", "employee@uit.edu.vn", "dateofbirth", "2000-01-01", "gender", "Nam", "address", "TP.HCM", "salary", "15000000", "status", "Đang làm", "is_deleted", "0"),
                true, user);
    }

    public static GenericManagementView areaView(AppUser user) {
        return view("Quản lý khu vực", "Tìm kiếm khu vực...", "Tổng khu vực", "Danh Sách Khu Vực",
                "area_management", "area_id", "KV", 3,
                List.of("area_id", "area_name", "area_type", "old_province", "boundary_wkt", "created_at", "is_deleted"),
                mapOf("area_id", "Mã Khu Vực", "area_name", "Tên Khu Vực", "area_type", "Loại Khu Vực", "old_province", "Tỉnh Cũ",
                        "boundary_wkt", "Ranh Giới WKT", "created_at", "Ngày Tạo", "is_deleted", "Đã Xóa"),
                mapOf("area_name", "Phường mới", "area_type", "Phường", "old_province", "TP.HCM",
                        "boundary_wkt", "POLYGON((106.700 10.770,106.710 10.770,106.710 10.780,106.700 10.780,106.700 10.770))",
                        "created_at", "2026-05-20", "is_deleted", "0"),
                true, user);
    }

    public static GenericManagementView areaBoundaryView(AppUser user) {
        return view("Quản lý ranh giới khu vực", "Tìm kiếm ranh giới...", "Tổng ranh giới", "Danh Sách Ranh Giới Khu Vực",
                "area_boundary", "area_id", "KV", 3,
                List.of("area_id", "boundary_wkt"),
                mapOf("area_id", "Mã Khu Vực", "boundary_wkt", "Ranh Giới WKT"),
                mapOf("boundary_wkt", "POLYGON((106.700 10.770,106.710 10.770,106.710 10.780,106.700 10.780,106.700 10.770))"),
                true, user);
    }

    public static GenericManagementView streetView(AppUser user) {
        return view("Quản lý tuyến đường", "Tìm kiếm tuyến đường...", "Tổng tuyến đường", "Danh Sách Tuyến Đường",
                "street", "street_id", "ST", 3,
                List.of("street_id", "street_name", "street_type", "road_level", "created_at", "is_deleted"),
                mapOf("street_id", "Mã Tuyến Đường", "street_name", "Tên Tuyến Đường", "street_type", "Loại Đường", "road_level", "Cấp Đường", "created_at", "Ngày Tạo", "is_deleted", "Đã Xóa"),
                mapOf("street_name", "Đường mới", "street_type", "Đường đô thị", "road_level", "2", "created_at", "2026-05-20", "is_deleted", "0"),
                true, user);
    }

    public static GenericManagementView segmentView(AppUser user) {
        return view("Quản lý đoạn đường", "Tìm kiếm đoạn đường...", "Tổng đoạn đường", "Danh Sách Đoạn Đường",
                "segment", "segment_id", "SG", 3,
                List.of("segment_id", "street_id", "area_id", "start_node_id", "end_node_id", "segment_length", "max_velocity", "created_at", "updated_at", "is_deleted"),
                mapOf("segment_id", "Mã Đoạn", "street_id", "Mã Tuyến", "area_id", "Mã Khu Vực", "start_node_id", "Nút Đầu", "end_node_id", "Nút Cuối", "segment_length", "Chiều Dài", "max_velocity", "Tốc Độ Tối Đa", "created_at", "Ngày Tạo", "updated_at", "Ngày Cập Nhật", "is_deleted", "Đã Xóa"),
                mapOf("street_id", "ST001", "area_id", "KV001", "start_node_id", "ND001", "end_node_id", "ND002", "segment_length", "1000", "max_velocity", "50", "created_at", "2026-05-20", "updated_at", "2026-05-20", "is_deleted", "0"),
                true, user);
    }

    public static GenericManagementView nodeView(AppUser user) {
        return view("Quản lý nút giao", "Tìm kiếm nút giao...", "Tổng nút giao", "Danh Sách Nút Giao",
                "node", "node_id", "ND", 3,
                List.of("node_id", "segment_id", "latitude", "longitude", "is_delete"),
                mapOf("node_id", "Mã Nút", "segment_id", "Mã Đoạn", "latitude", "Vĩ Độ", "longitude", "Kinh Độ", "is_delete", "Đã Xóa"),
                mapOf("segment_id", "SG001", "latitude", "10.7765000", "longitude", "106.7009000", "is_delete", "0"),
                true, user);
    }

    public static GenericManagementView segmentStatusView(AppUser user) {
        return view("Quản lý lưu lượng", "Tìm kiếm dữ liệu lưu lượng...", "Tổng bản ghi", "Danh Sách Lưu Lượng Giao Thông",
                "segment_status", "status_id", "SS", 3,
                List.of("status_id", "segment_id", "updated_at", "velocity", "traffic_level", "source_name"),
                mapOf("status_id", "Mã Trạng Thái", "segment_id", "Mã Đoạn", "updated_at", "Thời Điểm", "velocity", "Vận Tốc", "traffic_level", "Mức Lưu Lượng", "source_name", "Nguồn Dữ Liệu"),
                mapOf("segment_id", "SG001", "updated_at", "2026-05-20 08:00:00", "velocity", "30", "traffic_level", "FREE", "source_name", "segment_status.csv"),
                true, user);
    }

    public static GenericManagementView systemLogView(AppUser user) {
        return view("Quản lý nhật ký hệ thống", "Tìm kiếm nhật ký...", "Tổng nhật ký", "Danh Sách Nhật Ký Hệ Thống",
                "system_log", "log_id", "LG", 3,
                List.of("log_id", "account_id", "behaviour", "target_table", "target_id", "old_value", "new_value", "log_status", "created_at"),
                mapOf("log_id", "Mã Log", "account_id", "Mã Tài Khoản", "behaviour", "Hành Vi", "target_table", "Bảng Tác Động", "target_id", "Mã Dữ Liệu", "old_value", "Giá Trị Cũ", "new_value", "Giá Trị Mới", "log_status", "Kết Quả", "created_at", "Thời Gian"),
                mapOf("account_id", "AC001", "behaviour", "Thao tác hệ thống", "target_table", "ACCOUNT", "target_id", "AC001", "old_value", "", "new_value", "", "log_status", "SUCCESS", "created_at", "2026-05-20 08:00:00"),
                true, user);
    }

    public static GenericManagementView trafficMonitoringView(AppUser user) {
        return segmentStatusView(user);
    }

    public static GenericManagementView dataAnalysisView(AppUser user) {
        return segmentStatusView(user);
    }

    private static GenericManagementView view(String pageTitle, String searchPrompt, String statisticTitle, String tableTitle,
                                              String tableName, String primaryKeyColumn, String idPrefix, int idDigits,
                                              List<String> columns, Map<String, String> columnTitles, Map<String, String> defaultValues,
                                              boolean editable, AppUser user) {
        return new GenericManagementView(pageTitle, searchPrompt, statisticTitle, tableTitle, tableName, primaryKeyColumn,
                idPrefix, idDigits, columns, columnTitles, defaultValues, editable, user.getUsername());
    }

    private static Map<String, String> mapOf(String... values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }
}
