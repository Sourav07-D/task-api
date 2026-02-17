package com.example.task_api.service;

import com.example.task_api.dto.*;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.mapper.TaskMapper;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.task_api.exception.CustomNotFoundException;


import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class TaskService {
    private final UserRepository userRepository;


    private final TaskRepository repo;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "createdAt", "completed");


    private static final Logger log = LoggerFactory.getLogger(TaskService.class);


    // ✅ Create Task
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        log.info("Creating task for userId: {}", dto.getUserId());

        // ✅ referential integrity check
        if (!userRepository.existsById(dto.getUserId())) {
            throw new BadRequestException("User does not exist");
        }

        Task task = TaskMapper.fromCreateDTO(dto);

        Task saved = repo.save(task);

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

    public PagedResponseDTO<TaskResponseDTO> getTasksPaged(
            int page,
            int size,
            String sortBy,
            String direction) {

        log.info("Fetching paged tasks → page: {}, size: {}", page, size);

        // ✅ sort direction logic
        Sort.Direction dir =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String safeSort = resolveSortField(sortBy);

        Sort sort = Sort.by(dir, safeSort);


        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage = repo.findAll(pageable);

        // ✅ mapper discipline — no entity leakage
        List<TaskResponseDTO> dtoList =
                TaskMapper.toResponseList(taskPage.getContent());

        return new PagedResponseDTO<>(
                dtoList,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }
    public PagedResponseDTO<TaskResponseDTO> getTasksByCompletedPaged(
            boolean completed,
            int page,
            int size,
            String sortBy,
            String direction) {

        log.info("Fetching paged tasks by completed={} → page={}, size={}",
                completed, page, size);

        Sort.Direction dir =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String safeSort = resolveSortField(sortBy);

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, safeSort));


        Page<Task> taskPage =
                repo.findByCompleted(completed, pageable);

        List<TaskResponseDTO> dtoList =
                TaskMapper.toResponseList(taskPage.getContent());

        return new PagedResponseDTO<>(
                dtoList,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    private String resolveSortField(String sortBy) {

        if (ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return sortBy;
        }

        log.warn("Invalid sort field '{}', falling back to createdAt", sortBy);
        return "createdAt";
    }

    public List<TaskResponseDTO> getTasksByUser(String userId) {

        log.info("Fetching tasks for userId: {}", userId);

        // ✅ relation integrity guard (recommended)
        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException("User not found with id: " + userId);
        }

        List<Task> tasks = repo.findByUserId(userId);

        return TaskMapper.toResponseList(tasks);
    }


    public List<TaskResponseDTO> filterTasksByUser(
            String userId,
            Boolean completed,
            String keyword) {

        log.info("User filter routing → userId={}, completed={}, keyword={}",
                userId, completed, keyword);

        // ✅ relation integrity guard
        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException(
                    "User not found with id: " + userId);
        }

        List<Task> tasks;

        // ✅ decision routing
        if (completed != null && keyword != null) {

            // combined
            tasks = repo
                    .findByUserIdAndTitleContainingIgnoreCase(userId, keyword)
                    .stream()
                    .filter(t -> t.isCompleted() == completed)
                    .toList();

        } else if (completed != null) {

            // status only
            tasks = repo.findByUserIdAndCompleted(userId, completed);

        } else if (keyword != null && !keyword.isBlank()) {

            // keyword only
            tasks = repo.findByUserIdAndTitleContainingIgnoreCase(
                    userId, keyword);

        } else {

            // none
            tasks = repo.findByUserId(userId);
        }

        // ✅ mapper discipline preserved
        return TaskMapper.toResponseList(tasks);
    }


}

