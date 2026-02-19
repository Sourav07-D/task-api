package com.example.task_api.service;

import com.example.task_api.dto.*;
import com.example.task_api.exception.BadRequestException;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.mapper.TaskMapper;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskListProjection;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository repo;

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("title", "createdAt", "completed");

    // ✅ Create
    public TaskResponseDTO createTask(TaskRequestDTO dto) {

        if (!userRepository.existsById(dto.getUserId())) {
            throw new BadRequestException("User does not exist");
        }

        Task task = TaskMapper.fromCreateDTO(dto);
        return TaskMapper.toResponseDTO(repo.save(task));
    }

    // ✅ Get
    public List<TaskResponseDTO> getAllTasks() {
         return TaskMapper.toResponseList(repo.findAll())
                .stream()
                .map(this::enrichWithUser)
                .toList();

    }

    public TaskResponseDTO getTaskById(String id) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found"));
        TaskResponseDTO dto = TaskMapper.toResponseDTO(task);
        return enrichWithUser(dto);

    }

    // ✅ Delete
    public void deleteTask(String id) {
        if (!repo.existsById(id)) {
            throw new CustomNotFoundException("Task not found");
        }
        repo.deleteById(id);
    }

    // ✅ PUT with audit
    public TaskResponseDTO updateTask(
            String id,
            TaskUpdateDTO dto,
            String actingUserId) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found"));

        TaskMapper.mergeUpdate(task, dto);
        TaskMapper.touchAuditOnUpdate(task, actingUserId);

        return TaskMapper.toResponseDTO(repo.save(task));
    }

    // ✅ PATCH — status
    public TaskResponseDTO patchStatus(
            String id,
            TaskStatusPatchDTO dto,
            String actingUserId) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found"));

        TaskMapper.updateStatus(task, dto);
        TaskMapper.touchAuditOnUpdate(task, actingUserId);

        return TaskMapper.toResponseDTO(repo.save(task));
    }

    // ✅ PATCH — title
    public TaskResponseDTO patchTitle(
            String id,
            TaskTitlePatchDTO dto,
            String actingUserId) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found"));

        if (task.isCompleted()) {
            throw new BadRequestException("Completed tasks cannot change title");
        }

        TaskMapper.updateTitle(task, dto);
        TaskMapper.touchAuditOnUpdate(task, actingUserId);

        return TaskMapper.toResponseDTO(repo.save(task));
    }

    // ✅ PATCH — description
    public TaskResponseDTO patchDescription(
            String id,
            TaskDescriptionPatchDTO dto,
            String actingUserId) {

        Task task = repo.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Task not found"));

        TaskMapper.updateDescription(task, dto);
        TaskMapper.touchAuditOnUpdate(task, actingUserId);

        return TaskMapper.toResponseDTO(repo.save(task));
    }

    // ✅ User queries
    public List<TaskResponseDTO> getTasksByUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException("User not found");
        }
        return TaskMapper.toResponseList(repo.findByUserId(userId));
    }

    public List<TaskResponseDTO> filterTasksByUser(
            String userId,
            Boolean completed,
            String keyword) {

        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException("User not found");
        }

        List<Task> tasks;

        if (completed != null && keyword != null) {
            tasks = repo.findByUserIdAndTitleContainingIgnoreCase(userId, keyword)
                    .stream()
                    .filter(t -> t.isCompleted() == completed)
                    .toList();
        } else if (completed != null) {
            tasks = repo.findByUserIdAndCompleted(userId, completed);
        } else if (keyword != null && !keyword.isBlank()) {
            tasks = repo.findByUserIdAndTitleContainingIgnoreCase(userId, keyword);
        } else {
            tasks = repo.findByUserId(userId);
        }

        return TaskMapper.toResponseList(tasks);
    }

    // ✅ Paging
    public PagedResponseDTO<TaskResponseDTO> getTasksPaged(
            int page, int size, String sortBy, String direction) {

        Sort.Direction dir =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy)
                ? sortBy : "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, safeSort));

        Page<Task> taskPage = repo.findAll(pageable);

        return new PagedResponseDTO<>(
                TaskMapper.toResponseList(taskPage.getContent())
                        .stream()
                        .map(this::enrichWithUser)
                        .toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }
    private TaskResponseDTO enrichWithUser(TaskResponseDTO dto) {

        if (dto == null || dto.getUserId() == null) {
            return dto;
        }

        userRepository.findById(dto.getUserId())
                .map(com.example.task_api.mapper.UserMapper::toSummaryDTO)
                .ifPresent(dto::setUser);

        return dto;
    }
    public List<TaskListProjection> getTasksLightweightByUser(String userId) {

        log.info("Fetching lightweight tasks for userId: {}", userId);

        // Optional integrity check (recommended)
        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException(
                    "User not found with id: " + userId);
        }

        return repo.findProjectedByUserId(userId);
    }

    public PagedResponseDTO<TaskListProjection>
    getTasksLightweightPagedByUser(
            String userId,
            int page,
            int size) {

        log.info("Fetching paged lightweight tasks for userId: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new CustomNotFoundException(
                    "User not found with id: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<TaskListProjection> taskPage =
                repo.findProjectedByUserId(userId, pageable);

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
