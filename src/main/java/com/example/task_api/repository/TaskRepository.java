package com.example.task_api.repository;

import com.example.task_api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByTitle(String title);

    List<Task> findByCompleted(boolean completed);

    List<Task> findByTitleContainingIgnoreCase(String keyword);



    List<Task> findByCompletedTrue();

    List<Task> findByCompletedFalse();


    List<Task> findByTitleStartingWithIgnoreCase(String prefix);

    List<Task> findByTitleEndingWithIgnoreCase(String suffix);


    List<Task> findByTitleContainingIgnoreCaseAndCompleted(
            String keyword,
            boolean completed
    );
    Page<Task> findByCompleted(boolean completed, Pageable pageable);

    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndCompleted(String userId, boolean completed);

    List<Task> findByUserIdAndTitleContainingIgnoreCase(
            String userId,
            String keyword
    );
    List<TaskListProjection> findProjectedByUserId(String userId);
    Page<TaskListProjection> findProjectedByUserId(
            String userId,
            Pageable pageable
    );



}
