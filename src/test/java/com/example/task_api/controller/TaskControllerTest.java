package com.example.task_api.controller;

import com.example.task_api.dto.TaskResponseDTO;
import com.example.task_api.exception.CustomNotFoundException;   // ✅ ADDED
import com.example.task_api.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TaskService taskService;

    @Autowired
    ObjectMapper objectMapper;


    // ✅ ================================
    // ✅ SUCCESS CASE TEST — EXPANDED
    // ✅ ================================
    @Test
    void getTaskById_shouldReturnSuccessResponse() throws Exception {

        TaskResponseDTO dto = new TaskResponseDTO(
                "id1",
                "Learn Spring",
                "desc",
                false,
                LocalDateTime.now()
        );

        when(taskService.getTaskById("id1"))
                .thenReturn(dto);

        mockMvc.perform(get("/tasks/id1"))

                // ✅ status check
                .andExpect(status().isOk())

                // ✅ ApiResponse envelope checks
                .andExpect(jsonPath("$.success").value(true))     // already had
                .andExpect(jsonPath("$.message").exists())        // ✅ ADDED
                .andExpect(jsonPath("$.data").exists())           // ✅ ADDED

                // ✅ nested DTO checks — ADDED
                .andExpect(jsonPath("$.data.id").value("id1"))
                .andExpect(jsonPath("$.data.title").value("Learn Spring"))
                .andExpect(jsonPath("$.data.description").value("desc"))
                .andExpect(jsonPath("$.data.completed").value(false));
    }



    // ✅ ================================
    // ✅ ERROR CASE TEST — ADDED
    // ✅ ================================
    @Test
    void getTaskById_notFound_shouldReturnErrorJson() throws Exception {

        when(taskService.getTaskById("bad"))
                .thenThrow(new CustomNotFoundException("Task not found"));

        mockMvc.perform(get("/tasks/bad"))

                // ✅ status from GlobalExceptionHandler
                .andExpect(status().isNotFound())

                // ✅ error JSON structure checks
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}
