package com.example.task_api.constants;

public final class ApiMessages {

    private ApiMessages() {}

    // ✅ Create / Read
    public static final String TASK_CREATED = "Task created";
    public static final String TASK_FETCHED = "Task fetched";
    public static final String TASKS_FETCHED = "Tasks fetched";
    public static final String TASK_FOUND = "Task found";
    public static final String NO_TASKS_FOUND = "No tasks found";

    // ✅ Update / Delete
    public static final String TASK_UPDATED = "Task updated";
    public static final String TASK_DELETED = "Task deleted";

    // ✅ Patch operations
    public static final String TASK_STATUS_UPDATED = "Task status updated";
    public static final String TASK_TITLE_UPDATED = "Task title updated";
    public static final String TASK_DESCRIPTION_UPDATED = "Task description updated";

    // ✅ Other operations
    public static final String TASK_MARKED_COMPLETE = "Task marked complete";
    public static final String TASK_EXISTENCE_CHECKED = "Task existence checked";
    public static final String TASK_FILTERED = "Filtered tasks fetched";
    public static final String TASK_COUNT_FETCHED = "Task count fetched";
}

