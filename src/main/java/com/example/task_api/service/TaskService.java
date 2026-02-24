package com.example.task_api.service;

import com.example.task_api.dto.*;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.mapper.TaskMapper;
import com.example.task_api.mapper.UserMapper;
import com.example.task_api.model.Task;
import com.example.task_api.model.User;
import com.example.task_api.repository.TaskListProjection;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository repo;

    // ⭐ CHANGE — use UserService instead of repository
    // ensures USER CACHE is used
    private final UserService userService;

    private static final Logger log =
            LoggerFactory.getLogger(TaskService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "createdAt", "completed");

    // =====================================================
    // COMMON HELPERS
    // =====================================================

    private Task getTaskOrThrow(String id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new CustomNotFoundException(
                                "Task not found with id: " + id));
    }

    private void validateUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException(
                    "User not found with id: " + userId);
        }
    }

    private Task saveWithAudit(Task task, String actingUserId) {
        TaskMapper.touchAuditOnUpdate(task, actingUserId);
        return repo.save(task);
    }

    // =====================================================
    // ⭐ CHANGE — N+1 FIX (Batch Fetch Users)
    // =====================================================

    private Map<String, User> fetchUsersForTasks(List<Task> tasks) {

        List<String> userIds = tasks.stream()
                .map(Task::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.info("Batch fetching users count: {}", userIds.size());

        List<User> users =
                userRepository.findAllById(userIds);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u
                ));
    }

    // ⭐ CHANGE — replaces old enrichment loop
    private List<TaskResponseDTO> mapAndBatchEnrich(List<Task> tasks) {

        Map<String, User> userMap =
                fetchUsersForTasks(tasks);

        return tasks.stream()
                .map(task -> {

                    TaskResponseDTO dto =
                            TaskMapper.toResponseDTO(task);

                    User user =
                            userMap.get(task.getUserId());

                    if (user != null) {
                        dto.setUser(
                                UserMapper.toSummaryDTO(user));
                    }

                    return dto;
                })
                .toList();
    }

    // ⭐ CHANGE — single fetch now uses USER CACHE
    private TaskResponseDTO mapAndEnrich(Task task) {

        TaskResponseDTO dto =
                TaskMapper.toResponseDTO(task);

        // ⭐ IMPORTANT CHANGE
        // uses cached service call
        dto.setUser(
                userService.getUserSummary(task.getUserId())
        );

        return dto;
    }

    // =====================================================
    // CREATE
    // =====================================================

    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        log.info("Creating task for userId: {}", dto.getUserId());

        validateUserExists(dto.getUserId());

        Task task = TaskMapper.fromCreateDTO(dto);

        return mapAndEnrich(repo.save(task));
    }

    // =====================================================
    // READ
    // =====================================================

    public List<TaskResponseDTO> getAllTasks() {
        return mapAndBatchEnrich(repo.findAll());
    }

    // ⭐ CHANGE — TASK CACHE ENABLED
    @Cacheable(value = "tasks", key = "#id")
    public TaskResponseDTO getTaskById(String id) {

        log.info("Fetching task from DB with id: {}", id);

        return mapAndEnrich(getTaskOrThrow(id));
    }

    // =====================================================
    // DELETE
    // =====================================================

    // ⭐ CHANGE — CACHE EVICTION ADDED
    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(String id) {

        log.info("Deleting task {}, cache evicted", id);

        repo.delete(getTaskOrThrow(id));
    }

    // =====================================================
    // UPDATE
    // =====================================================

    // ⭐ CHANGE — CACHE EVICTION
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponseDTO updateTask(
            String id,
            TaskUpdateDTO dto,
            String actingUserId) {

        log.info("Updating task {}, cache evicted", id);

        Task task = getTaskOrThrow(id);

        TaskMapper.mergeUpdate(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    // ⭐ CHANGE — CACHE EVICTION
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponseDTO patchStatus(
            String id,
            TaskStatusPatchDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        TaskMapper.updateStatus(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    // ⭐ CHANGE — CACHE EVICTION
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponseDTO patchTitle(
            String id,
            TaskTitlePatchDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        if (task.isCompleted()) {
            throw new BadRequestException(
                    "Completed tasks cannot change title");
        }

        TaskMapper.updateTitle(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    // ⭐ CHANGE — CACHE EVICTION
    @CacheEvict(value = "tasks", key = "#id")
    public TaskResponseDTO patchDescription(
            String id,
            TaskDescriptionPatchDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        TaskMapper.updateDescription(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    // =====================================================
    // USER QUERIES (N+1 OPTIMIZED)
    // =====================================================

    public List<TaskResponseDTO> getTasksByUser(String userId) {

        validateUserExists(userId);

        return mapAndBatchEnrich(
                repo.findByUserId(userId));
    }

    // =====================================================
    // PAGINATION
    // =====================================================

    public PagedResponseDTO<TaskResponseDTO>
    getTasksPaged(int page,
                  int size,
                  String sortBy,
                  String direction) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                "desc".equalsIgnoreCase(direction)
                                        ? Sort.Direction.DESC
                                        : Sort.Direction.ASC,
                                ALLOWED_SORT_FIELDS.contains(sortBy)
                                        ? sortBy
                                        : "createdAt"
                        ));

        Page<Task> taskPage =
                repo.findAll(pageable);

        return new PagedResponseDTO<>(
                mapAndBatchEnrich(taskPage.getContent()),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    // =====================================================
    // PROJECTION (ALREADY OPTIMAL)
    // =====================================================

    public List<TaskListProjection>
    getTasksLightweightByUser(String userId) {

        validateUserExists(userId);

        return repo.findProjectedByUserId(userId);
    }

    public PagedResponseDTO<TaskListProjection>
    getTasksLightweightPagedByUser(
            String userId,
            int page,
            int size) {

        validateUserExists(userId);

        Pageable pageable =
                PageRequest.of(page, size);

        Page<TaskListProjection> taskPage =
                repo.findProjectedByUserId(
                        userId, pageable);

        return new PagedResponseDTO<>(
                taskPage.getContent(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }
}