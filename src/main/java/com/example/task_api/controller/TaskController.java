package com.example.task_api.controller;

import com.example.task_api.constants.ApiMessages;
import com.example.task_api.dto.*;
import com.example.task_api.repository.TaskListProjection;
import com.example.task_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    // ✅ Create
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(
            @Valid @RequestBody TaskRequestDTO dto) {

        TaskResponseDTO response = service.createTask(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_CREATED)
                        .data(response)
                        .build()
        );
    }

    // ✅ Get by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> getById(@PathVariable String id) {

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_FETCHED)
                        .data(service.getTaskById(id))
                        .build()
        );
    }

    // ✅ Get all
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getAllTasks() {

        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .success(true)
                        .message(ApiMessages.TASKS_FETCHED)
                        .data(service.getAllTasks())
                        .build()
        );
    }

    // ✅ Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable String id) {

        service.deleteTask(id);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message(ApiMessages.TASK_DELETED)
                        .data(null)
                        .build()
        );
    }

    // ✅ PUT — audit-enabled (ONLY ONE PUT — duplicate removed)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskUpdateDTO dto,
            @RequestHeader("X-User-Id") String actingUserId) {

        TaskResponseDTO updated =
                service.updateTask(id, dto, actingUserId);

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_UPDATED)
                        .data(updated)
                        .build()
        );
    }

    // ✅ PATCH — status (audit-enabled)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> patchStatus(
            @PathVariable String id,
            @Valid @RequestBody TaskStatusPatchDTO dto,
            @RequestHeader("X-User-Id") String actingUserId) {

        TaskResponseDTO updated =
                service.patchStatus(id, dto, actingUserId);

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_STATUS_UPDATED)
                        .data(updated)
                        .build()
        );
    }

    // ✅ PATCH — title
    @PatchMapping("/{id}/title")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> patchTitle(
            @PathVariable String id,
            @Valid @RequestBody TaskTitlePatchDTO dto,
            @RequestHeader("X-User-Id") String actingUserId) {

        TaskResponseDTO updated =
                service.patchTitle(id, dto, actingUserId);

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_TITLE_UPDATED)
                        .data(updated)
                        .build()
        );
    }

    // ✅ PATCH — description
    @PatchMapping("/{id}/description")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> patchDescription(
            @PathVariable String id,
            @Valid @RequestBody TaskDescriptionPatchDTO dto,
            @RequestHeader("X-User-Id") String actingUserId) {

        TaskResponseDTO updated =
                service.patchDescription(id, dto, actingUserId);

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_DESCRIPTION_UPDATED)
                        .data(updated)
                        .build()
        );
    }

    // ✅ User relation queries
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getTasksByUser(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .success(true)
                        .message(ApiMessages.TASKS_FETCHED)
                        .data(service.getTasksByUser(userId))
                        .build()
        );
    }

    @GetMapping("/user/{userId}/filter")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> filterTasksByUser(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .success(true)
                        .message(ApiMessages.TASK_FILTERED)
                        .data(service.filterTasksByUser(userId, completed, keyword))
                        .build()
        );

    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<List<TaskListProjection>>>
    getTasksSummaryByUser(@PathVariable String userId) {

        List<TaskListProjection> list =
                service.getTasksLightweightByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<TaskListProjection>>builder()
                        .success(true)
                        .message("Task summaries fetched")
                        .data(list)
                        .build()
        );
    }
    @GetMapping("/user/{userId}/summary/paged")
    public ResponseEntity<ApiResponse<PagedResponseDTO<TaskListProjection>>>
    getTasksSummaryPagedByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        PagedResponseDTO<TaskListProjection> response =
                service.getTasksLightweightPagedByUser(
                        userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponseDTO<TaskListProjection>>builder()
                        .success(true)
                        .message("Paged task summaries fetched")
                        .data(response)
                        .build()
        );
    }


}
