package com.example.task_api.service;

import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.exception.CustomNotFoundException;
import com.example.task_api.mapper.TaskMapper;
import com.example.task_api.model.Task;
import com.example.task_api.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repo;

    @InjectMocks
    TaskService service;

    @Test
    void getTaskById_success() {

        Task task = new Task(
                "id1",
                "Title",
                "Desc",
                false,
                LocalDateTime.now()
        );

        when(repo.findById("id1"))
                .thenReturn(Optional.of(task));

        TaskResponseDTO dto =
                service.getTaskById("id1");

        assertNotNull(dto);
        assertEquals("Title", dto.getTitle());

        verify(repo).findById("id1");
    }

    @Test
    void getTaskById_notFound_shouldThrow() {

        when(repo.findById("bad"))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomNotFoundException.class,
                () -> service.getTaskById("bad")
        );

        verify(repo).findById("bad");
    }
}

