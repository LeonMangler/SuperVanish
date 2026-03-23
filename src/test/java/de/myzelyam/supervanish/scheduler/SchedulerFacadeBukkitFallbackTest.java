package de.myzelyam.supervanish.scheduler;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchedulerFacadeBukkitFallbackTest {

    private JavaPlugin plugin;
    private Server server;
    private BukkitScheduler scheduler;
    private SchedulerFacade facade;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(JavaPlugin.class);
        server = Mockito.mock(Server.class);
        scheduler = Mockito.mock(BukkitScheduler.class);

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(server.getScheduler()).thenReturn(scheduler);
        when(scheduler.runTaskLater(eq(plugin), any(Runnable.class), anyLong())).thenReturn(Mockito.mock(BukkitTask.class));
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong())).thenReturn(Mockito.mock(BukkitTask.class));

        facade = new SchedulerFacade(plugin);
    }

    @Test
    void runLaterGlobalUsesBukkitScheduler() {
        facade.runLaterGlobal(() -> { }, 5L);

        verify(scheduler, times(1)).runTaskLater(eq(plugin), any(Runnable.class), eq(5L));
    }

    @Test
    void runLaterForEntityUsesBukkitSchedulerFallback() {
        Entity entity = Mockito.mock(Entity.class);

        facade.runLaterForEntity(entity, () -> { }, 3L);

        verify(scheduler, times(1)).runTaskLater(eq(plugin), any(Runnable.class), eq(3L));
    }

    @Test
    void cancelAllCancelsTrackedRepeatingTasks() {
        BukkitTask task = Mockito.mock(BukkitTask.class);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

        facade.runGlobalRepeating(() -> { }, 0L, 20L);
        facade.cancelAll();

        verify(task, times(1)).cancel();
    }
}
