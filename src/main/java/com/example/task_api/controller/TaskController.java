package com.example.task_api.controller;

import com.example.task_api.dto.TaskRequestDTO;
import com.example.task_api.dto.TaskResponseDTO;
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
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO dto) {

        TaskResponseDTO response = service.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // ✅ Get All Tasks
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        return ResponseEntity.ok(service.getAllTasks());
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getTaskById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        service.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskRequestDTO dto) {

        return ResponseEntity.ok(service.updateTask(id, dto));
    }


    @GetMapping("/search")
    public List<TaskResponseDTO> searchTasks(@RequestParam String keyword) {
        return service.searchByTitle(keyword);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponseDTO> markComplete(@PathVariable String id) {
        return ResponseEntity.ok(service.markCompleted(id));
    }



}

