package de.myzelyam.supervanish.scheduler;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class SchedulerFacade {

    private static final long MILLIS_PER_TICK = 50L;

    private final JavaPlugin plugin;
    private final Server server;
    private final PlatformMode platformMode;
    private final Set<TaskHandle> activeTasks = ConcurrentHashMap.newKeySet();

    public SchedulerFacade(JavaPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.platformMode = detectPlatformMode(server);
    }

    public PlatformMode getPlatformMode() {
        return platformMode;
    }

    public boolean isFolia() {
        return platformMode == PlatformMode.FOLIA;
    }

    public void cancelAll() {
        for (TaskHandle task : activeTasks.toArray(new TaskHandle[0])) {
            try {
                task.cancel();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to cancel scheduled task", e);
            }
        }
        activeTasks.clear();
    }

    public TaskHandle runNowGlobal(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return scheduleOneShotBukkit(taskRunnable -> server.getScheduler().runTask(plugin, taskRunnable), runnable);
        }
        try {
            Object scheduler = invoke(server, "getGlobalRegionScheduler");
            return scheduleOneShotFolia(consumer -> invoke(scheduler, "run", plugin, consumer), runnable);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule global Folia task", e);
        }
    }

    public TaskHandle runLaterGlobal(Runnable runnable, long delayTicks) {
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return scheduleOneShotBukkit(taskRunnable -> server.getScheduler().runTaskLater(plugin, taskRunnable, delayTicks), runnable);
        }
        try {
            Object scheduler = invoke(server, "getGlobalRegionScheduler");
            return scheduleOneShotFolia(consumer -> invoke(scheduler, "runDelayed", plugin, consumer, delayTicks), runnable);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule delayed Folia task", e);
        }
    }

    public TaskHandle runGlobalRepeating(Runnable runnable, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return track(new BukkitTaskHandle(server.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks)));
        }
        try {
            Object scheduler = invoke(server, "getGlobalRegionScheduler");
            Object task = invoke(scheduler, "runAtFixedRate", plugin, consumerOf(taskHandle -> runnable.run()),
                    initialDelayTicks, periodTicks);
            return track(new ReflectiveTaskHandle(task));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule repeating Folia global task", e);
        }
    }

    public TaskHandle runAsyncRepeating(Runnable runnable, long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return track(new BukkitTaskHandle(server.getScheduler().runTaskTimerAsynchronously(plugin, runnable,
                    initialDelayTicks, periodTicks)));
        }
        try {
            Object scheduler = invoke(server, "getAsyncScheduler");
            Object task = invoke(scheduler, "runAtFixedRate", plugin, consumerOf(taskHandle -> runnable.run()),
                    ticksToMillis(initialDelayTicks), ticksToMillis(periodTicks), TimeUnit.MILLISECONDS);
            return track(new ReflectiveTaskHandle(task));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule Folia async task", e);
        }
    }

    public TaskHandle runForEntity(Entity entity, Runnable runnable) {
        if (isOwnedByCurrentRegion(entity)) {
            runnable.run();
            return NoOpTaskHandle.INSTANCE;
        }
        return runLaterForEntity(entity, runnable, 1L);
    }

    public TaskHandle runLaterForEntity(Entity entity, Runnable runnable, long delayTicks) {
        return runLaterForEntity(entity, runnable, null, delayTicks);
    }

    public TaskHandle runLaterForEntity(Entity entity, Runnable runnable, Runnable retiredTask, long delayTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return scheduleOneShotBukkit(taskRunnable -> server.getScheduler().runTaskLater(plugin, taskRunnable, delayTicks), runnable);
        }
        try {
            Object scheduler = invoke(entity, "getScheduler");
            final TaskHandle[] holder = new TaskHandle[1];
            Consumer<Object> wrappedConsumer = consumerOf(task -> {
                try {
                    runnable.run();
                } finally {
                    removeTracked(holder[0]);
                }
            });
            TaskHandle delegate = new ReflectiveTaskHandle(
                    invoke(scheduler, "run", plugin, wrappedConsumer, wrapRetired(holder, retiredTask), delayTicks));
            holder[0] = delegate;
            return track(delegate);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule Folia entity task", e);
        }
    }

    public TaskHandle runRepeatingForEntity(Entity entity, Runnable runnable, Runnable retiredTask,
                                            long initialDelayTicks, long periodTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return track(new BukkitTaskHandle(server.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks,
                    periodTicks)));
        }
        try {
            Object scheduler = invoke(entity, "getScheduler");
            final TaskHandle[] holder = new TaskHandle[1];
            TaskHandle delegate = new ReflectiveTaskHandle(invoke(scheduler, "runAtFixedRate", plugin,
                    consumerOf(taskHandle -> runnable.run()), wrapRetired(holder, retiredTask), initialDelayTicks,
                    periodTicks));
            holder[0] = delegate;
            return track(delegate);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule Folia entity repeating task", e);
        }
    }

    public TaskHandle runAtLocation(Location location, Runnable runnable) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(runnable, "runnable");
        if (!isFolia()) {
            return runNowGlobal(runnable);
        }
        try {
            Object scheduler = invoke(server, "getRegionScheduler");
            return scheduleOneShotFolia(consumer -> invoke(scheduler, "run", plugin, location, consumer), runnable);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule Folia region task", e);
        }
    }

    private boolean isOwnedByCurrentRegion(Entity entity) {
        if (!isFolia()) {
            return true;
        }
        try {
            Object result = invoke(server, "isOwnedByCurrentRegion", entity);
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private TaskHandle track(TaskHandle taskHandle) {
        if (taskHandle == NoOpTaskHandle.INSTANCE) {
            return taskHandle;
        }
        activeTasks.add(taskHandle);
        return new ManagedTaskHandle(taskHandle);
    }

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * MILLIS_PER_TICK;
    }

    private TaskHandle scheduleOneShotBukkit(BukkitTaskScheduler scheduler, Runnable runnable) {
        final TaskHandle[] holder = new TaskHandle[1];
        Runnable wrapped = () -> {
            try {
                runnable.run();
            } finally {
                removeTracked(holder[0]);
            }
        };
        TaskHandle delegate = new BukkitTaskHandle(scheduler.schedule(wrapped));
        holder[0] = delegate;
        return track(delegate);
    }

    private TaskHandle scheduleOneShotFolia(FoliaTaskScheduler scheduler, Runnable runnable) throws ReflectiveOperationException {
        final TaskHandle[] holder = new TaskHandle[1];
        Consumer<Object> wrappedConsumer = consumerOf(task -> {
            try {
                runnable.run();
            } finally {
                removeTracked(holder[0]);
            }
        });
        TaskHandle delegate = new ReflectiveTaskHandle(scheduler.schedule(wrappedConsumer));
        holder[0] = delegate;
        return track(delegate);
    }

    private Runnable wrapRetired(TaskHandle[] holder, Runnable retiredTask) {
        return () -> {
            try {
                if (retiredTask != null) {
                    retiredTask.run();
                }
            } finally {
                removeTracked(holder[0]);
            }
        };
    }

    private void removeTracked(TaskHandle taskHandle) {
        if (taskHandle != null) {
            activeTasks.remove(taskHandle);
        }
    }

    public static PlatformMode detectPlatformMode(Server server) {
        try {
            server.getClass().getMethod("getGlobalRegionScheduler");
            server.getClass().getMethod("getAsyncScheduler");
            return PlatformMode.FOLIA;
        } catch (NoSuchMethodException e) {
            return PlatformMode.BUKKIT;
        }
    }

    private static Object invoke(Object instance, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(instance.getClass(), methodName, args);
        try {
            return method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ReflectiveOperationException) {
                throw (ReflectiveOperationException) cause;
            }
            throw new IllegalStateException("Invocation failed for method " + methodName, cause);
        }
    }

    private static Method findMethod(Class<?> type, String name, Object[] args) throws NoSuchMethodException {
        Method matched = null;
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }
            boolean compatible = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (args[i] == null) {
                    continue;
                }
                if (!wrap(parameterTypes[i]).isAssignableFrom(wrap(args[i].getClass()))) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                matched = method;
                break;
            }
        }
        if (matched == null) {
            throw new NoSuchMethodException(type.getName() + "#" + name);
        }
        matched.setAccessible(true);
        return matched;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static Consumer<Object> consumerOf(Consumer<Object> consumer) {
        return consumer;
    }

    @FunctionalInterface
    private interface BukkitTaskScheduler {
        BukkitTask schedule(Runnable runnable);
    }

    @FunctionalInterface
    private interface FoliaTaskScheduler {
        Object schedule(Consumer<Object> consumer) throws ReflectiveOperationException;
    }

    private final class ManagedTaskHandle implements TaskHandle {
        private final TaskHandle delegate;

        private ManagedTaskHandle(TaskHandle delegate) {
            this.delegate = delegate;
        }

        @Override
        public void cancel() {
            try {
                delegate.cancel();
            } finally {
                activeTasks.remove(delegate);
                activeTasks.remove(this);
            }
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }
    }

    private static final class BukkitTaskHandle implements TaskHandle {
        private final BukkitTask task;

        private BukkitTaskHandle(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private static final class ReflectiveTaskHandle implements TaskHandle {
        private final Object task;

        private ReflectiveTaskHandle(Object task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            try {
                invoke(task, "cancel");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to cancel Folia task", e);
            }
        }

        @Override
        public boolean isCancelled() {
            try {
                Object result = invoke(task, "isCancelled");
                return result instanceof Boolean && (Boolean) result;
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }
    }

    private enum NoOpTaskHandle implements TaskHandle {
        INSTANCE;

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }
    }
}
