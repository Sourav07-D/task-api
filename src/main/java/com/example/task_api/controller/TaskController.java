package com.example.task_api.controller;

import com.example.task_api.dto.TaskRequestDTO;
import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    // ✅ Create Task
    @PostMapping
    public TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO dto) {
        return service.createTask(dto);
    }

    // ✅ Get All Tasks
    @GetMapping
    public List<TaskResponseDTO> getAllTasks() {
        return service.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getById(@PathVariable String id) {
        return service.getTaskById(id);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable String id) {
        return service.deleteTask(id);
    }

    @PutMapping("/{id}")
    public TaskResponseDTO updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskRequestDTO dto) {

        return service.updateTask(id, dto);
    }

    @GetMapping("/search")
    public List<TaskResponseDTO> searchTasks(@RequestParam String keyword) {
        return service.searchByTitle(keyword);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponseDTO markComplete(@PathVariable String id) {
        return service.markCompleted(id);
    }


}

