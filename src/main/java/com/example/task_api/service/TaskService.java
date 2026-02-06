package com.example.task_api.service;

import com.example.task_api.dto.TaskRequestDTO;
import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.task_api.exception.CustomNotFoundException;


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

    public TaskResponseDTO getTaskById(String id) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));

        return mapToResponse(task);
    }

    public String deleteTask(String id) {

        boolean exists = repo.existsById(id);

        if (!exists) {
            throw new CustomNotFoundException("Task not found with id: " + id);
        }


        repo.deleteById(id);

        return "Task deleted successfully with id: " + id;
    }

    public TaskResponseDTO updateTask(String id, TaskRequestDTO dto) {

        // 1️⃣ Fetch existing
        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));


        // 2️⃣ Update mutable fields only
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        // DO NOT change:
        // task.setId(...)
        // task.setCreatedAt(...)

        // 3️⃣ Save updated document
        Task updated = repo.save(task);

        // 4️⃣ Return DTO
        return mapToResponse(updated);
    }

    public List<TaskResponseDTO> searchByTitle(String keyword) {

        List<Task> tasks = repo.findByTitleContainingIgnoreCase(keyword);

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponseDTO markCompleted(String id) {

        // fetch existing
        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));


        // partial update
        task.setCompleted(true);

        // save
        Task updated = repo.save(task);

        return mapToResponse(updated);
    }

    public boolean taskExists(String id) {
        return repo.existsById(id);
    }

    public List<TaskResponseDTO> filterTasks(String keyword, Boolean completed) {

        List<Task> tasks;

        if (keyword != null && completed != null) {
            // both filters present
            tasks = repo.findByTitleContainingIgnoreCaseAndCompleted(keyword, completed);

        } else if (keyword != null) {
            // only keyword
            tasks = repo.findByTitleContainingIgnoreCase(keyword);

        } else if (completed != null) {
            // only completed flag
            tasks = completed
                    ? repo.findByCompletedTrue()
                    : repo.findByCompletedFalse();

        } else {
            // no filters
            tasks = repo.findAll();
        }

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long getTaskCount() {
        return repo.count();
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

