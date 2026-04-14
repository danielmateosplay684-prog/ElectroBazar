package com.proconsi.electrobazar.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Simple service to track progress of long-running admin tasks.
 */
@Service
public class TaskProgressService {

    private final Map<String, ProgressInfo> tasks = new ConcurrentHashMap<>();

    public void updateProgress(String taskId, int percentage, String message) {
        tasks.put(taskId, new ProgressInfo(percentage, message));
    }

    public ProgressInfo getProgress(String taskId) {
        return tasks.getOrDefault(taskId, new ProgressInfo(0, "Iniciando..."));
    }

    public void finishTask(String taskId) {
        tasks.remove(taskId);
    }

    public static record ProgressInfo(int percentage, String message) {}
}
