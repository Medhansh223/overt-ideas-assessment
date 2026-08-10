package com.overt.assessment.taskqueue.repository;

import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    List<Task> findByStatus(TaskStatus status);
}
