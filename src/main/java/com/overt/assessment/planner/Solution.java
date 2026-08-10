package com.overt.assessment.planner;

import java.util.List;

public class Solution {
    public static void main(String[] args) {
        DependencyPlanner planner = new DependencyPlanner();

        // Create tasks matching the PDF example
        TaskNode design = new TaskNode("design", List.of());
        TaskNode api = new TaskNode("api", List.of("design"));
        TaskNode ui = new TaskNode("ui", List.of("design"));
        TaskNode release = new TaskNode("release", List.of("api", "ui"));

        List<TaskNode> tasks = List.of(design, api, ui, release);

        // Run topological sort
        List<String> order = planner.planExecutionOrder(tasks);

        System.out.println("Valid execution order is: " + String.join(", ", order));
    }
}
