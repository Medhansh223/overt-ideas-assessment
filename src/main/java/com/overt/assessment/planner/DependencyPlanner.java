package com.overt.assessment.planner;

import com.overt.assessment.planner.constant.DependencyPlannerConstants;
import com.overt.assessment.planner.exception.CircularDependencyException;
import com.overt.assessment.planner.exception.DependencyPlannerException;
import com.overt.assessment.planner.exception.MissingDependencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handles dependency planning logic by executing topological sorting.
 */
public final class DependencyPlanner {

    private static final Logger logger = LoggerFactory.getLogger(DependencyPlanner.class);

    // States for three-color DFS cycle detection
    private enum NodeState {
        UNVISITED, // White
        VISITING,  // Grey (currently in the recursion stack)
        VISITED    // Black (fully processed)
    }

    /**
     * Plans a valid execution order of tasks based on their dependencies.
     * Time Complexity: O(V + E)
     * Space Complexity: O(V + E)
     *
     * @param tasks The list of tasks and their dependencies.
     * @return A list of task IDs in a valid execution order.
     * @throws DependencyPlannerException if cyclic or missing dependencies are detected.
     */
    public List<String> planExecutionOrder(List<TaskNode> tasks) {
        logger.info("Initializing dependency planning for {} tasks", tasks == null ? 0 : tasks.size());
        if (tasks == null) {
            logger.error("Failed to plan dependencies: input task list is null");
            throw new DependencyPlannerException(DependencyPlannerConstants.TASK_LIST_CANNOT_BE_NULL);
        }

        Map<String, TaskNode> taskMap = new HashMap<>();
        for (TaskNode task : tasks) {
            if (taskMap.containsKey(task.id())) {
                String errorMsg = DependencyPlannerConstants.TASK_IDS_MUST_BE_UNIQUE + task.id();
                logger.error(errorMsg);
                throw new DependencyPlannerException(errorMsg);
            }
            taskMap.put(task.id(), task);
        }

        Map<String, NodeState> stateMap = new HashMap<>();
        for (String taskId : taskMap.keySet()) {
            stateMap.put(taskId, NodeState.UNVISITED);
        }

        List<String> executionOrder = new ArrayList<>();

        for (String taskId : taskMap.keySet()) {
            if (stateMap.get(taskId) == NodeState.UNVISITED) {
                depthFirstSearch(taskId, taskMap, stateMap, executionOrder);
            }
        }

        logger.info("Successfully planned execution order: {}", executionOrder);
        return executionOrder;
    }

    private void depthFirstSearch(String nodeId, Map<String, TaskNode> taskMap, Map<String, NodeState> stateMap, List<String> executionOrder) {
        stateMap.put(nodeId, NodeState.VISITING);

        TaskNode task = taskMap.get(nodeId);
        if (task != null && task.dependsOn() != null) {
            for (String dependencyId : task.dependsOn()) {
                if (!taskMap.containsKey(dependencyId)) {
                    String errorMsg = DependencyPlannerConstants.MISSING_DEPENDENCY_DETECTED + dependencyId;
                    logger.warn("Validation failure: {} is missing. Referenced by task {}", dependencyId, nodeId);
                    throw new MissingDependencyException(errorMsg);
                }

                NodeState depState = stateMap.get(dependencyId);
                if (depState == NodeState.VISITING) {
                    String errorMsg = DependencyPlannerConstants.CYCLIC_DEPENDENCY_DETECTED + nodeId + " -> " + dependencyId;
                    logger.error("Cyclic dependency loop detected: {}", errorMsg);
                    throw new CircularDependencyException(errorMsg);
                }

                if (depState == NodeState.UNVISITED) {
                    depthFirstSearch(dependencyId, taskMap, stateMap, executionOrder);
                }
            }
        }

        stateMap.put(nodeId, NodeState.VISITED);
        executionOrder.add(nodeId);
    }
}
