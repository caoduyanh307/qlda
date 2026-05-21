package com.ddangkhoa.trafficfx.view;

import com.ddangkhoa.trafficfx.model.Role;

import java.util.List;

public final class RoleFeatures {
    private RoleFeatures() {}

    public static List<String> featuresFor(Role role) {
        return switch (role) {
            case ADMIN -> List.of(
                    "Quản lý nhân viên",
                    "Quản lý tài khoản",
                    "Quản lý nhật ký hệ thống"
            );
            case TECHNICIAN -> List.of(
                    "Quản lý khu vực",
                    "Quản lý tuyến đường",
                    "Quản lý đoạn đường",
                    "Quản lý nút giao",
                    "Quản lý lưu lượng"
            );
            case ANALYST -> List.of(
                    "Giám sát giao thông",
                    "Quản lý phân tích dữ liệu"
            );
        };
    }

    public static String roleDescription(Role role) {
        return switch (role) {
            case ADMIN -> "Theo dõi tài khoản, phân quyền, nhân sự và nhật ký thao tác của hệ thống.";
            case TECHNICIAN -> "Quản lý dữ liệu nền như khu vực, tuyến đường, đoạn đường, nút giao và lưu lượng.";
            case ANALYST -> "Theo dõi dữ liệu lưu lượng và khai thác dữ liệu phục vụ phân tích giao thông.";
        };
    }
}
