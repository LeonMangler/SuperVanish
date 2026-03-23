package de.myzelyam.supervanish.scheduler;

public interface TaskHandle {

    void cancel();

    default boolean isCancelled() {
        return false;
    }
}
