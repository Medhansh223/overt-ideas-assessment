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

    public List<String> planExecutionOrder(List<TaskNode> tasks) {
        logger.info("Initializing dependency planning for {} tasks", tasks == null ? 0 : tasks.size());
        if (tasks == null) {
            logger.error("Failed to plan dependencies: input task list is null");
            throw new DependencyPlannerException(DependencyPlannerConstants.TASK_LIST_CANNOT_BE_NULL);
        }

        int n = tasks.size();

        // Map task IDs to integer indices (0 to n-1)
        Map<String, Integer> idToIndexMap = new HashMap<>(); // api -> 0
        List<String> indexToIdMap = new ArrayList<>(); // 0 -> api
        for (int i = 0; i < n; i++) {
            String taskId = tasks.get(i).id();
            if (idToIndexMap.containsKey(taskId)) {
                String errorMsg = DependencyPlannerConstants.TASK_IDS_MUST_BE_UNIQUE + taskId;
                logger.error(errorMsg);
                throw new DependencyPlannerException(errorMsg);
            }
            idToIndexMap.put(taskId, i);
            indexToIdMap.add(taskId);
        }

        // Initialize adjacency list using your structure: ArrayList<ArrayList<Integer>>
        ArrayList<ArrayList<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        // Populate adjacency list
        for (int i = 0; i < n; i++) {
            TaskNode task = tasks.get(i);
            for (String depId : task.dependsOn()) {
                if (!idToIndexMap.containsKey(depId)) {
                    String errorMsg = DependencyPlannerConstants.MISSING_DEPENDENCY_DETECTED + depId;
                    logger.warn("Validation failure: {} is missing. Referenced by task {}", depId, task.id());
                    throw new MissingDependencyException(errorMsg);
                }
                int depIndex = idToIndexMap.get(depId);
                // Edge points from dependency to the task (dependency must be completed first)
                adj.get(depIndex).add(i);
            }
        }

        // Initialize stack, visited vector (vis), and recursion tracking (visiting) for cycle safety
        Stack<Integer> st = new Stack<>();
        int[] vis = new int[n];
        int[] visiting = new int[n]; // 1 if in recursion stack, 0 otherwise

        // Run topological sort
        for (int i = 0; i < n; i++) {
            if (vis[i] == 0) {
                findTopoSort(i, vis, visiting, adj, st, indexToIdMap);
            }
        }

        // Pop elements from Stack into the result vector (topo)
        ArrayList<String> topo = new ArrayList<>();
        while (!st.isEmpty()) {
            topo.add(indexToIdMap.get(st.pop()));
        }

        logger.info("Successfully planned execution order: {}", topo);
        return topo;
    }

    private void findTopoSort(int node, int[] vis, int[] visiting, ArrayList<ArrayList<Integer>> adj, Stack<Integer> st, List<String> indexToIdMap) {
        vis[node] = 1;
        visiting[node] = 1; // Mark as visiting for cycle detection

        for (Integer neighbor : adj.get(node)) {
            if (visiting[neighbor] == 1) {
                String errorMsg = DependencyPlannerConstants.CYCLIC_DEPENDENCY_DETECTED + indexToIdMap.get(node) + " -> " + indexToIdMap.get(neighbor);
                logger.error("Cyclic dependency loop detected: {}", errorMsg);
                throw new CircularDependencyException(errorMsg);
            }
            if (vis[neighbor] == 0) {
                findTopoSort(neighbor, vis, visiting, adj, st, indexToIdMap);
            }
        }

        visiting[node] = 0; // Backtrack
        st.push(node);
    }
}
