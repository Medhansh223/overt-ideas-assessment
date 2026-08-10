package com.overt.assessment.taskqueue.repository;

import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByStatus(TaskStatus status);
}
