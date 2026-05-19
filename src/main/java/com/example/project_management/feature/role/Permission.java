package com.example.project_management.feature.role;

/**
 * Danh sách các quyền hạn (Permissions) trong hệ thống.
 * Mỗi Role có thể được gán một tập hợp các Permission khác nhau.
 */
public enum Permission {

    // ===== Quyền về Người dùng =====
    VIEW_USERS("Xem danh sách người dùng"),
    MANAGE_USERS("Quản lý người dùng"),

    // ===== Quyền về Chức vụ =====
    VIEW_ROLES("Xem danh sách chức vụ"),
    MANAGE_ROLES("Quản lý chức vụ (CRUD)"),

    // ===== Quyền về Dự án =====
    VIEW_ALL_PROJECTS("Xem tất cả dự án"),
    CREATE_PROJECT("Tạo dự án"),
    EDIT_PROJECT("Chỉnh sửa dự án"),
    DELETE_PROJECT("Xóa dự án"),
    MANAGE_PROJECT_MEMBERS("Quản lý thành viên dự án"),

    // ===== Quyền về Công việc (Task) =====
    VIEW_TASKS("Xem công việc"),
    CREATE_TASK("Tạo công việc"),
    EDIT_TASK("Chỉnh sửa công việc"),
    DELETE_TASK("Xóa công việc"),
    ASSIGN_TASK("Phân công công việc"),

    // ===== Quyền về Bình luận =====
    CREATE_COMMENT("Tạo bình luận"),
    DELETE_ANY_COMMENT("Xóa bình luận của người khác"),

    // ===== Quyền về Thống kê =====
    VIEW_DASHBOARD("Xem trang tổng quan"),
    VIEW_REPORTS("Xem báo cáo thống kê");

    private final String displayName;

    Permission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
