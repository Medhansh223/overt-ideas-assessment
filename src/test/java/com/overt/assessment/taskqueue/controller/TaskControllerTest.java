package com.overt.assessment.taskqueue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import com.overt.assessment.taskqueue.dto.TaskRequestDto;
import com.overt.assessment.taskqueue.dto.TaskResponseDto;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import com.overt.assessment.taskqueue.exception.TaskNotFoundException;
import com.overt.assessment.taskqueue.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void shouldCreateTaskSuccessfully() throws Exception {
        // Arrange
        TaskRequestDto request = new TaskRequestDto("task-1", "Implement API", TaskPriority.HIGH, TaskStatus.PENDING, null, 4.0);
        TaskResponseDto response = new TaskResponseDto("task-1", "Implement API", TaskPriority.HIGH, TaskStatus.PENDING, null, 4.0, LocalDateTime.now());

        when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(TaskQueueConstants.API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.title").value("Implement API"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsMissing() throws Exception {
        // Arrange
        TaskRequestDto invalidRequest = new TaskRequestDto("task-1", "", TaskPriority.HIGH, TaskStatus.PENDING, null, 4.0);

        // Act & Assert
        mockMvc.perform(post(TaskQueueConstants.API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").value(TaskQueueConstants.FIELD_REQUIRED_TITLE));
    }

    @Test
    void shouldListTasksSuccessfully() throws Exception {
        // Arrange
        TaskResponseDto response = new TaskResponseDto("task-1", "Implement API", TaskPriority.HIGH, TaskStatus.PENDING, null, 4.0, LocalDateTime.now());
        when(taskService.listTasks(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        // Act & Assert
        mockMvc.perform(get(TaskQueueConstants.API_BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("task-1"))
                .andExpect(jsonPath("$.content[0].title").value("Implement API"));
    }

    @Test
    void shouldRecommendNextTaskSuccessfully() throws Exception {
        // Arrange
        TaskResponseDto response = new TaskResponseDto("task-1", "Implement API", TaskPriority.HIGH, TaskStatus.PENDING, null, 4.0, LocalDateTime.now());
        when(taskService.recommendNextPendingTask()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(TaskQueueConstants.API_BASE_PATH + TaskQueueConstants.RECOMMENDED_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-1"));
    }

    @Test
    void shouldReturn404WhenNoTasksPendingToRecommend() throws Exception {
        // Arrange
        when(taskService.recommendNextPendingTask()).thenThrow(new TaskNotFoundException("No pending tasks found"));

        // Act & Assert
        mockMvc.perform(get(TaskQueueConstants.API_BASE_PATH + TaskQueueConstants.RECOMMENDED_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No pending tasks found"));
    }

    @Test
    void shouldDeleteTaskSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(TaskQueueConstants.API_BASE_PATH + "/task-1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask("task-1");
    }
}
