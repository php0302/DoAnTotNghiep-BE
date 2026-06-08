package com.example.project_management.feature.task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskStatusTransitionTest {

    @Test
    public void testValidTransitions() {
        // TODO -> IN_PROGRESS, BLOCKED
        assertTrue(TaskStatus.isValidTransition(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.TODO, TaskStatus.BLOCKED));
        
        // IN_PROGRESS -> IN_REVIEW, BLOCKED
        assertTrue(TaskStatus.isValidTransition(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED));

        // IN_REVIEW -> TESTING, IN_PROGRESS, BLOCKED
        assertTrue(TaskStatus.isValidTransition(TaskStatus.IN_REVIEW, TaskStatus.TESTING));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.IN_REVIEW, TaskStatus.IN_PROGRESS));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.IN_REVIEW, TaskStatus.BLOCKED));

        // TESTING -> DONE, IN_PROGRESS, BLOCKED
        assertTrue(TaskStatus.isValidTransition(TaskStatus.TESTING, TaskStatus.DONE));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.TESTING, TaskStatus.IN_PROGRESS));
        assertTrue(TaskStatus.isValidTransition(TaskStatus.TESTING, TaskStatus.BLOCKED));

        // BLOCKED -> IN_PROGRESS
        assertTrue(TaskStatus.isValidTransition(TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS));

        // DONE -> IN_PROGRESS
        assertTrue(TaskStatus.isValidTransition(TaskStatus.DONE, TaskStatus.IN_PROGRESS));

        // Same status transition (no-op) is always valid
        for (TaskStatus status : TaskStatus.values()) {
            assertTrue(TaskStatus.isValidTransition(status, status));
        }
    }

    @Test
    public void testInvalidTransitions() {
        // Forbidden explicitly
        assertFalse(TaskStatus.isValidTransition(TaskStatus.TODO, TaskStatus.DONE));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.TODO, TaskStatus.TESTING));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.DONE, TaskStatus.TODO));

        // Other invalid ones
        assertFalse(TaskStatus.isValidTransition(TaskStatus.TODO, TaskStatus.IN_REVIEW));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.BLOCKED, TaskStatus.DONE));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.BLOCKED, TaskStatus.TODO));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.BLOCKED, TaskStatus.IN_REVIEW));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.BLOCKED, TaskStatus.TESTING));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.DONE, TaskStatus.TESTING));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.DONE, TaskStatus.IN_REVIEW));
        assertFalse(TaskStatus.isValidTransition(TaskStatus.DONE, TaskStatus.BLOCKED));
    }
}
