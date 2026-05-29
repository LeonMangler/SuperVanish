/*
 * Copyright (c) 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import io.github.projectunified.minelib.scheduler.common.util.Platform;
import io.github.projectunified.minelib.scheduler.common.task.Task;
import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class FoliaUtil {

    private static final Method IS_OWNED_BY_CURRENT_REGION = findIsOwnedByCurrentRegion();

    private FoliaUtil() {
    }

    public static boolean isFolia() {
        return Platform.FOLIA.isPlatform();
    }

    public static Task runGlobal(Plugin plugin, Runnable runnable) {
        return GlobalScheduler.get(plugin).run(runnable);
    }

    public static Task runGlobalTimer(Plugin plugin, BooleanSupplier runnable, long delayTicks, long periodTicks) {
        return GlobalScheduler.get(plugin).runTimer(runnable, delayTicks, periodTicks);
    }

    public static Task runGlobalTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        return runGlobalTimer(plugin, () -> {
            runnable.run();
            return true;
        }, delayTicks, periodTicks);
    }

    public static boolean isOwnedByCurrentRegion(Entity entity) {
        if (!isFolia() || entity == null) return true;
        if (IS_OWNED_BY_CURRENT_REGION == null) return false;
        try {
            return (Boolean) IS_OWNED_BY_CURRENT_REGION.invoke(null, entity);
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return false;
        }
    }

    public static void runAtEntity(Plugin plugin, Entity entity, Runnable runnable) {
        if (entity == null) return;
        if (isOwnedByCurrentRegion(entity)) {
            runnable.run();
            return;
        }
        entity.getScheduler().run(plugin, task -> runnable.run(), () -> {});
    }

    public static void runAtEntityLater(Plugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (entity == null) return;
        if (!isFolia()) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            return;
        }
        entity.getScheduler().runDelayed(plugin, task -> runnable.run(), () -> {}, delayTicks);
    }

    public static List<Player> onlinePlayersSnapshot() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static void forEachOnlinePlayer(Plugin plugin, Consumer<Player> action) {
        for (Player player : onlinePlayersSnapshot()) {
            runAtEntity(plugin, player, () -> {
                if (player.isOnline()) action.accept(player);
            });
        }
    }

    private static Method findIsOwnedByCurrentRegion() {
        try {
            return Bukkit.class.getMethod("isOwnedByCurrentRegion", Entity.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
