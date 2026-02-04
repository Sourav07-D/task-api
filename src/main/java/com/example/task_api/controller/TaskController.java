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
}

