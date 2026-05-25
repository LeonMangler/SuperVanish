package de.myzelyam.supervanish.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FoliaScheduler implements TaskScheduler {
    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final RegionScheduler regionScheduler;
    private final AsyncScheduler asyncScheduler;
    private final ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.globalScheduler = Bukkit.getGlobalRegionScheduler();
        this.regionScheduler = Bukkit.getRegionScheduler();
        this.asyncScheduler = Bukkit.getAsyncScheduler();
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTask(Runnable task) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = globalScheduler.run(plugin, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        });
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskLater(Runnable task, long delay) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = globalScheduler.runDelayed(plugin, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        }, delay);
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskTimer(Runnable task, long delay, long period) {
        ScheduledTask scheduledTask = globalScheduler.runAtFixedRate(plugin, (t) -> task.run(), delay, period);
        int taskId = taskIdCounter.incrementAndGet();
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskAsynchronously(Runnable task) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = asyncScheduler.runNow(plugin, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        });
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskLaterAsynchronously(Runnable task, long delay) {
        long delayMs = delay * 50;
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = asyncScheduler.runDelayed(plugin, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        long delayMs = delay * 50;
        long periodMs = period * 50;
        ScheduledTask scheduledTask = asyncScheduler.runAtFixedRate(plugin, (t) -> task.run(), delayMs, periodMs, TimeUnit.MILLISECONDS);
        int taskId = taskIdCounter.incrementAndGet();
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskAtLocation(Location location, Runnable task) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = regionScheduler.run(plugin, location, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        });
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public de.myzelyam.supervanish.scheduler.ScheduledTask runTaskAtEntity(Entity entity, Runnable task) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = entity.getScheduler().run(plugin, (t) -> {
            try {
                task.run();
            } finally {
                tasks.remove(taskId);
            }
        }, null);
        tasks.put(taskId, scheduledTask);
        return wrapTask(taskId, scheduledTask);
    }

    @Override
    public void cancelAllTasks() {
        tasks.values().forEach(ScheduledTask::cancel);
        tasks.clear();
        globalScheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    private de.myzelyam.supervanish.scheduler.ScheduledTask wrapTask(int taskId, ScheduledTask foliaTask) {
        return new de.myzelyam.supervanish.scheduler.ScheduledTask() {
            @Override
            public void cancel() {
                tasks.remove(taskId);
                foliaTask.cancel();
            }

            @Override
            public boolean isCancelled() {
                return foliaTask.isCancelled();
            }
        };
    }
}
