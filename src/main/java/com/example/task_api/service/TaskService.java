package com.example.task_api.service;

import com.example.task_api.dto.*;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.mapper.TaskMapper;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.task_api.exception.CustomNotFoundException;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);


    // ✅ Create Task
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        Task task = TaskMapper.fromCreateDTO(dto);

        // createdAt auto-set in entity

        Task saved = repo.save(task);

        // Entity → ResponseDTO
        return TaskMapper.toResponseDTO(saved);
    }

    // ✅ Get All Tasks
    public List<TaskResponseDTO> getAllTasks() {
         return TaskMapper.toResponseList(repo.findAll());
    }

    public TaskResponseDTO getTaskById(String id) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));

        return TaskMapper.toResponseDTO(task);
    }

    public String deleteTask(String id) {

        boolean exists = repo.existsById(id);

        if (!exists) {
            throw new CustomNotFoundException("Task not found with id: " + id);
        }


        repo.deleteById(id);

        return "Task deleted successfully with id: " + id;
    }

    public TaskResponseDTO updateTask(String id, TaskUpdateDTO dto) {
        log.info("Updating task with id: {}", id);

        // 1️⃣ Fetch existing
        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));

        TaskMapper.mergeUpdate(task, dto);

        Task saved = repo.save(task);

        return TaskMapper.toResponseDTO(saved);
    }

    public List<TaskResponseDTO> searchByTitle(String keyword) {

        List<Task> tasks = repo.findByTitleContainingIgnoreCase(keyword);

        return TaskMapper.toResponseList(tasks);
    }

    public TaskResponseDTO markCompleted(String id) {

        // fetch existing
        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found with id: " + id));


        TaskStatusPatchDTO dto = new TaskStatusPatchDTO();
        dto.setCompleted(true);

        TaskMapper.updateStatus(task, dto);

        // save
        Task updated = repo.save(task);

        return TaskMapper.toResponseDTO(updated);
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

        return TaskMapper.toResponseList(tasks);
    }

    public long getTaskCount() {
        return repo.count();
    }


    public TaskResponseDTO patchStatus(String id, TaskStatusPatchDTO dto) {

        log.info("Task status changed → id: {}", id);

        Task task = repo.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException("Task not found with id: " + id));

        TaskMapper.updateStatus(task, dto);


        Task saved = repo.save(task);

        return TaskMapper.toResponseDTO(saved);
    }

    public TaskResponseDTO patchTitle(String id, TaskTitlePatchDTO dto) {

        log.info("Task title change requested → id: {}", id);

        Task task = repo.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException("Task not found with id: " + id));

        // ✅ Business rule enforcement
        if (task.isCompleted()) {
            throw new BadRequestException(
                    "Completed tasks cannot change title");
        }

        TaskMapper.updateTitle(task, dto);


        Task saved = repo.save(task);

        log.info("Task title changed → id: {}", id);

        return TaskMapper.toResponseDTO(saved);
    }


    public TaskResponseDTO patchDescription(String id, TaskDescriptionPatchDTO dto) {

        log.info("Task description changed → id: {}", id);

        Task task = repo.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException("Task not found with id: " + id));

        TaskMapper.updateDescription(task, dto);
        Task saved = repo.save(task);

        return TaskMapper.toResponseDTO(saved);
    }


}

