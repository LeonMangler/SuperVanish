package de.myzelyam.supervanish.scheduler;

import org.bukkit.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerFacadePlatformModeTest {

    @Test
    void detectPlatformModeReturnsBukkitWithoutFoliaMethods() {
        Server server = Mockito.mock(Server.class);

        assertEquals(PlatformMode.BUKKIT, SchedulerFacade.detectPlatformMode(server));
    }

    @Test
    void detectPlatformModeReturnsFoliaWhenServerExposesFoliaMethods() {
        Server server = Mockito.mock(Server.class, Mockito.withSettings().extraInterfaces(FoliaServerMethods.class));

        assertEquals(PlatformMode.FOLIA, SchedulerFacade.detectPlatformMode(server));
    }

    @SuppressWarnings("unused")
    private interface FoliaServerMethods {
        Object getGlobalRegionScheduler();

        Object getAsyncScheduler();
    }
}
