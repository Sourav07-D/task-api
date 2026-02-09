package com.example.task_api.controller;

import com.example.task_api.constants.ApiMessages;
import com.example.task_api.dto.ApiResponse;
import com.example.task_api.dto.TaskRequestDTO;
import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.dto.TaskUpdateDTO;
import com.example.task_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    // ✅ Create Task
    @PostMapping
    public  ResponseEntity<ApiResponse<TaskResponseDTO>> createTask(
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


    // ✅ Get All Tasks
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


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskUpdateDTO dto) {

        TaskResponseDTO updated = service.updateTask(id, dto);

        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_UPDATED)
                        .data(updated)
                        .build()
        );

    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> searchTasks(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .success(true)
                        .message(ApiMessages.TASKS_FETCHED)
                        .data(service.searchByTitle(keyword))
                        .build()
        );
    }


    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponseDTO>> markComplete(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.<TaskResponseDTO>builder()
                        .success(true)
                        .message(ApiMessages.TASK_MARKED_COMPLETE)
                        .data(service.markCompleted(id))
                        .build()
        );

    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<ApiResponse<Boolean>> taskExists(@PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .success(true)
                        .message(ApiMessages.TASK_EXISTENCE_CHECKED)
                        .data(service.taskExists(id))
                        .build()
        );

    }
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> filterTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean completed) {

        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .success(true)
                        .message(ApiMessages.TASK_FILTERED)
                        .data(service.filterTasks(keyword, completed))
                        .build()
        );

    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTaskCount() {
        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .success(true)
                        .message(ApiMessages.TASK_COUNT_FETCHED)
                        .data(service.getTaskCount())
                        .build()
        );

    }



}

