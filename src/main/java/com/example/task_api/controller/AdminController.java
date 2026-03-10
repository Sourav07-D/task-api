package com.example.task_api.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/v1/admin")
    public class AdminController {

        @DeleteMapping("/tasks/{id}")
        public String deleteTask(@PathVariable String id) {
            return "Admin deleted task with id: " + id;
        }
    }


