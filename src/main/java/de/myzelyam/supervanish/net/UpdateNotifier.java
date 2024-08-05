/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.net;

import de.myzelyam.supervanish.SuperVanish;

import io.github.projectunified.minelib.scheduler.async.AsyncScheduler;
import io.github.projectunified.minelib.scheduler.common.task.Task;
import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import lombok.Getter;

public class UpdateNotifier {

    // 20 minutes
    private static final long CHECK_INTERVAL = 20 * 60 * 20;

    private final SuperVanish plugin;
    private final Task checkTask;
    @Getter
    private final String currentVersion;
    private final Set<UUID> notifiedPlayers = new HashSet<>();
    @Getter
    private volatile String latestVersion;
    private boolean notifiedConsole = false;

    public UpdateNotifier(final SuperVanish plugin) {
        this.plugin = plugin;
        currentVersion = plugin.getDescription().getVersion();
        checkTask = start();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(final PlayerJoinEvent e) {
                GlobalScheduler.get(plugin).runLater(() -> {
                    if (!isUpToDate())
                        notifyPlayer(e.getPlayer());
                }, 2);
            }
        }, plugin);
    }

    public boolean isUpToDate() {
        try {
            if (latestVersion == null) throw new NumberFormatException();
            int comparision = plugin.getVersionUtil().compareVersions(currentVersion, latestVersion);
            return comparision >= 0;
        } catch (NumberFormatException e) {
            return currentVersion.equals(latestVersion);
        }
    }

    private void notifyPlayer(Player p) {
        if (p.hasPermission("sv.notify") && latestVersion != null) {
            if (notifiedPlayers.contains(p.getUniqueId())) return;
            notifiedPlayers.add(p.getUniqueId());
            plugin.sendMessage(p,
                    plugin.getMessage("PluginOutdated").replace("%new%", latestVersion)
                            .replace("%current%", currentVersion), p);
        }
    }

    private void notifyAdmins() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            notifyPlayer(p);
        }
    }

    private void notifyConsole() {
        if (!notifiedConsole) {
            plugin.log(Level.INFO, "Your current version of SuperVanish is outdated" +
                    " - New version: '" + latestVersion + "'; Currently: '" + currentVersion + "'");
            notifiedConsole = true;
        }
    }

    private Task start() {
        if (checkTask != null) throw new IllegalStateException("Task is already running");
        return AsyncScheduler.get(plugin).runTimer(() -> {
                String latestVersion = fetchLatestVersion();
                UpdateNotifier.this.latestVersion = latestVersion.equals("Error")
                        ? UpdateNotifier.this.latestVersion == null
                        ? currentVersion
                        : UpdateNotifier.this.latestVersion
                        : latestVersion;
                if (!isUpToDate())
                    GlobalScheduler.get(plugin).run(() -> {
                            notifyConsole();
                            if (plugin.getSettings().getBoolean(
                                    "MiscellaneousOptions.UpdateChecker.NotifyAdmins")) notifyAdmins();
                    });
        }, 0, CHECK_INTERVAL);
    }

    private String fetchLatestVersion() {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=1331").openConnection();
            con.setConnectTimeout(20 * 1000);
            con.setReadTimeout(20 * 1000);
            String version;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                version = reader.readLine();
            }
            con.disconnect();
            if (version.length() <= 7)
                return version;
            else throw new RuntimeException("'" + version + "' is not a valid version");
        } catch (Exception e) {
            plugin.log(Level.WARNING, "Failed to check for an update: "
                    + e.getMessage());
            return "Error";
        }
    }
}
