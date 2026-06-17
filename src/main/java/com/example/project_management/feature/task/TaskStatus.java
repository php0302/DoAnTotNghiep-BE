package com.example.project_management.feature.task;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    TESTING,
    DONE,
    BLOCKED;

    public String getDisplayName() {
        switch (this) {
            case TODO: return "Cần làm";
            case IN_PROGRESS: return "Đang làm";
            case IN_REVIEW: return "Chờ review";
            case TESTING: return "Đang test";
            case DONE: return "Hoàn thành";
            case BLOCKED: return "Tạm dừng";
            default: return this.name();
        }
    }

    public static boolean isValidTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) {
            return true;
        }

        // Cấm các chuyển đổi cụ thể được yêu cầu
        if (oldStatus == TODO && newStatus == DONE) return false;
        if (oldStatus == TODO && newStatus == TESTING) return false;
        if (oldStatus == DONE && newStatus == TODO) return false;

        switch (oldStatus) {
            case TODO:
                return newStatus == IN_PROGRESS || newStatus == BLOCKED;
            case IN_PROGRESS:
                return newStatus == IN_REVIEW || newStatus == BLOCKED;
            case IN_REVIEW:
                return newStatus == TESTING || newStatus == IN_PROGRESS || newStatus == BLOCKED;
            case TESTING:
                return newStatus == DONE || newStatus == IN_PROGRESS || newStatus == BLOCKED;
            case BLOCKED:
                return newStatus == IN_PROGRESS;
            case DONE:
                return newStatus == IN_PROGRESS;
            default:
                return false;
        }
    }
}

