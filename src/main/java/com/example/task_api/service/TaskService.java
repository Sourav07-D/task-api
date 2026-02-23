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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository repo;

    private static final Logger log =
            LoggerFactory.getLogger(TaskService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "createdAt", "completed");

    // =========================================================
    // ✅ COMMON HELPERS
    // =========================================================

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

    // =========================================================
    // ⭐⭐⭐ NEW — BATCH FETCH USERS (N+1 FIX)
    // =========================================================

    /**
     * CHANGE ⭐
     * Fetch all required users in ONE DB query
     * instead of fetching per task.
     */
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
                        user -> user
                ));
    }

    /**
     * CHANGE ⭐
     * Replaces OLD mapAndEnrichList()
     * Eliminates N+1 queries.
     */
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
                                UserMapper.toSummaryDTO(user)
                        );
                    }

                    return dto;
                })
                .toList();
    }

    /**
     * SINGLE OBJECT enrichment (OK — not N+1)
     */
    private TaskResponseDTO mapAndEnrich(Task task) {

        TaskResponseDTO dto =
                TaskMapper.toResponseDTO(task);

        userRepository.findById(task.getUserId())
                .map(UserMapper::toSummaryDTO)
                .ifPresent(dto::setUser);

        return dto;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        log.info("Creating task for userId: {}",
                dto.getUserId());

        validateUserExists(dto.getUserId());

        Task task =
                TaskMapper.fromCreateDTO(dto);

        return mapAndEnrich(repo.save(task));
    }

    // =========================================================
    // READ
    // =========================================================

    /**
     * CHANGE ⭐
     * OLD → mapAndEnrichList()
     * NEW → mapAndBatchEnrich()
     */
    public List<TaskResponseDTO> getAllTasks() {

        List<Task> tasks = repo.findAll();

        return mapAndBatchEnrich(tasks);
    }

    public TaskResponseDTO getTaskById(String id) {
        return mapAndEnrich(getTaskOrThrow(id));
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteTask(String id) {
        repo.delete(getTaskOrThrow(id));
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public TaskResponseDTO updateTask(
            String id,
            TaskUpdateDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        TaskMapper.mergeUpdate(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    public TaskResponseDTO patchStatus(
            String id,
            TaskStatusPatchDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        TaskMapper.updateStatus(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

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

    public TaskResponseDTO patchDescription(
            String id,
            TaskDescriptionPatchDTO dto,
            String actingUserId) {

        Task task = getTaskOrThrow(id);

        TaskMapper.updateDescription(task, dto);

        return mapAndEnrich(
                saveWithAudit(task, actingUserId));
    }

    // =========================================================
    // USER QUERIES
    // =========================================================

    /**
     * CHANGE ⭐ N+1 FIX APPLIED
     */
    public List<TaskResponseDTO> getTasksByUser(
            String userId) {

        validateUserExists(userId);

        List<Task> tasks =
                repo.findByUserId(userId);

        return mapAndBatchEnrich(tasks);
    }

    /**
     * CHANGE ⭐ N+1 FIX APPLIED
     */
    public List<TaskResponseDTO> filterTasksByUser(
            String userId,
            Boolean completed,
            String keyword) {

        validateUserExists(userId);

        List<Task> tasks;

        if (completed != null && keyword != null) {

            tasks = repo
                    .findByUserIdAndTitleContainingIgnoreCase(
                            userId, keyword)
                    .stream()
                    .filter(t ->
                            t.isCompleted() == completed)
                    .toList();

        } else if (completed != null) {

            tasks =
                    repo.findByUserIdAndCompleted(
                            userId, completed);

        } else if (keyword != null &&
                !keyword.isBlank()) {

            tasks =
                    repo.findByUserIdAndTitleContainingIgnoreCase(
                            userId, keyword);

        } else {

            tasks =
                    repo.findByUserId(userId);
        }

        return mapAndBatchEnrich(tasks);
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    /**
     * CHANGE ⭐ Pagination also optimized
     */
    public PagedResponseDTO<TaskResponseDTO>
    getTasksPaged(
            int page,
            int size,
            String sortBy,
            String direction) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        "desc".equalsIgnoreCase(direction)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC,
                        ALLOWED_SORT_FIELDS.contains(sortBy)
                                ? sortBy
                                : "createdAt"
                )
        );

        Page<Task> taskPage =
                repo.findAll(pageable);

        return new PagedResponseDTO<>(
                mapAndBatchEnrich(
                        taskPage.getContent()),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    // =========================================================
    // PROJECTION (UNCHANGED — ALREADY OPTIMAL)
    // =========================================================

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