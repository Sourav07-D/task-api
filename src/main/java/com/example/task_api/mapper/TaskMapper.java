package com.example.task_api.mapper;

import com.example.task_api.dto.*;
import com.example.task_api.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {

    public static TaskResponseDTO toResponseDTO(Task entity) {

        if (entity == null) {
            return null;
        }

        TaskResponseDTO dto = new TaskResponseDTO();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCompleted(entity.isCompleted());
        dto.setCreatedAt(entity.getCreatedAt());

       dto.setUserId(entity.getUserId());


        return dto;
    }
    public static Task fromCreateDTO(TaskRequestDTO dto) {

        if (dto == null) {
            return null;
        }

        Task task = new Task();

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        task.setUserId(dto.getUserId());


        // ✅ system defaults
        task.setCompleted(false);
        task.setCreatedAt(LocalDateTime.now());

        return task;
    }
    public static void mergeUpdate(Task entity, TaskUpdateDTO dto) {

        if (entity == null || dto == null) {
            return;
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }


    public static void updateTitle(Task entity, TaskTitlePatchDTO dto) {

        if (entity == null || dto == null) {
            return;
        }

        entity.setTitle(dto.getTitle());
    }



    public static void updateDescription(Task entity, TaskDescriptionPatchDTO dto) {

        if (entity == null || dto == null) {
            return;
        }

        entity.setDescription(dto.getDescription());
    }


    public static void updateStatus(Task entity, TaskStatusPatchDTO dto) {

        if (entity == null || dto == null) {
            return;
        }

        entity.setCompleted(dto.getCompleted());
    }

    public static List<TaskResponseDTO> toResponseList(List<Task> entities) {

        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(TaskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


}

