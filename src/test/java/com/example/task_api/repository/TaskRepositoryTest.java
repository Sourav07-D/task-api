package com.example.task_api.repository;

import com.example.task_api.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void save_and_findById_shouldWork() {

        Task task = new Task();
        task.setTitle("Repo Test Title");
        task.setDescription("Repo Test Desc");
        task.setCompleted(false);
        task.setCreatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        Optional<Task> found =
                taskRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Repo Test Title", found.get().getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnResult() {

        Task task = new Task();
        task.setTitle("Spring Boot Testing");
        task.setDescription("desc");
        task.setCompleted(false);
        task.setCreatedAt(LocalDateTime.now());

        taskRepository.save(task);

        var results =
                taskRepository
                        .findByTitleContainingIgnoreCase("spring");

        assertFalse(results.isEmpty());
    }
}

