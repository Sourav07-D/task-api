package com.example.task_api.service;

import com.example.task_api.dto.TaskRequestDTO;
import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;

    // ✅ Create Task
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        // DTO → Entity (manual mapping)
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(false);   // force default

        // createdAt auto-set in entity

        Task saved = repo.save(task);

        // Entity → ResponseDTO
        return mapToResponse(saved);
    }

    // ✅ Get All Tasks
    public List<TaskResponseDTO> getAllTasks() {
        return repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ Manual mapper method (important for learning)
    private TaskResponseDTO mapToResponse(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .createdAt(task.getCreatedAt())
                .build();
    }
}

