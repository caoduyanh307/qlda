package com.ddangkhoa.trafficfx.controller;

import java.util.List;

import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.view.AccountInfoView;
import com.ddangkhoa.trafficfx.view.ManagementPage;
import com.ddangkhoa.trafficfx.view.RoleFeatures;
import com.ddangkhoa.trafficfx.view.management.ManagementViewFactory;

import javafx.scene.Parent;

public class DashboardController {
    public List<String> getFeatures(AppUser user) {
        return RoleFeatures.featuresFor(user.getRole());
    }

    public String getIconFor(String feature) {
        return switch (feature) {
            case "Quản lý khu vực" -> "⌂";
            case "Quản lý tuyến đường" -> "🛣";
            case "Quản lý đoạn đường" -> "▥";
            case "Quản lý nút giao" -> "⌖";
            case "Quản lý lưu lượng" -> "≋";
            case "Quản lý nhân viên" -> "👤";
            case "Quản lý tài khoản" -> "⚙";
            case "Quản lý nhật ký hệ thống" -> "☰";
            case "Thông tin tài khoản" -> "👤";
            case "Giám sát giao thông" -> "◉";
            case "Quản lý phân tích dữ liệu" -> "▣";
            default -> "•";
        };
    }
    
    public Parent createFeatureView(String feature, AppUser user) {
        return switch (feature) {
            case "Thông tin tài khoản" -> new AccountInfoView(user).getView();
            case "Quản lý khu vực" -> ManagementViewFactory.areaView(user).getView();
            case "Quản lý tuyến đường" -> ManagementViewFactory.streetView(user).getView();
            case "Quản lý đoạn đường" -> ManagementViewFactory.segmentView(user).getView();
            case "Quản lý nút giao" -> ManagementViewFactory.nodeView(user).getView();
            case "Quản lý lưu lượng" -> ManagementViewFactory.segmentStatusView(user).getView();
            case "Quản lý nhân viên" -> ManagementViewFactory.employeeView(user).getView();
            case "Quản lý tài khoản" -> ManagementViewFactory.accountView(user).getView();
            case "Quản lý nhật ký hệ thống" -> ManagementViewFactory.systemLogView(user).getView();
            case "Giám sát giao thông" -> ManagementViewFactory.trafficMonitoringView(user).getView();
            case "Quản lý phân tích dữ liệu" -> ManagementViewFactory.dataAnalysisView(user).getView();
            default -> new ManagementPage(feature, user).getView();
        };
    }
}
