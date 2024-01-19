/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.PostPlayerHideEvent;
import de.myzelyam.api.vanish.PostPlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.Validation;
import de.myzelyam.supervanish.visibility.hiders.PlayerHider;
import lombok.Getter;
import me.hsgamer.hscore.bukkit.folia.FoliaChecker;
import me.hsgamer.hscore.bukkit.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.logging.Level;

public class VisibilityChanger {

    @Getter
    private final PlayerHider hider;
    private final SuperVanish plugin;
    private final FileConfiguration config;

    public VisibilityChanger(PlayerHider hider, SuperVanish plugin) {
        this.plugin = plugin;
        this.hider = hider;
        config = plugin.getSettings();
    }

    public void hidePlayer(Player player) {
        hidePlayer(player, null, false);
    }

    public void showPlayer(Player player) {
        showPlayer(player, null, false);
    }

    public void hidePlayer(Player player, String hiderName) {
        hidePlayer(player, hiderName, false);
    }

    public void showPlayer(Player player, String hiderName) {
        showPlayer(player, hiderName, false);
    }

    public void hidePlayer(final Player player, final String hiderName, boolean silent) {
        try {
            Validation.checkNotNull("player cannot be null", player);
            if (plugin.getVanishStateMgr().isVanished(player.getUniqueId())) {
                plugin.log(Level.WARNING, "Failed to hide " + player.getName()
                        + " since that player is already invisible.");
                return;
            }
            // call event
            PlayerHideEvent e = new PlayerHideEvent(player, silent);
            plugin.getServer().getPluginManager().callEvent(e);
            if (e.isCancelled()) return;
            silent = e.isSilent();
            // state
            plugin.getVanishStateMgr().setVanishedState(player.getUniqueId(),
                    player.getName(), true, hiderName);
            // metadata
            player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            // hide
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                if (!plugin.hasPermissionToSee(onlinePlayer, player))
                    plugin.getVisibilityChanger().getHider().setHidden(player, onlinePlayer, true);
            // fly check
            if (config.getBoolean("InvisibilityFeatures.Fly.Enable")) {
                player.setAllowFlight(true);
            }
            // action bars
            if (plugin.getActionBarMgr() != null && config.getBoolean("MessageOptions.DisplayActionBar")) {
                plugin.getActionBarMgr().addActionBar(player);
            }
            // sleep state
            player.setSleepingIgnored(true);
            // chat message
            if (hiderName == null)
                plugin.sendMessage(player, "OnVanish", player);
            else
                plugin.sendMessage(player, "OnVanishCausedByOtherPlayer", player, hiderName);
            // stop player from being a mob target
            if (config.getBoolean("InvisibilityFeatures.DisableMobTarget")) {
                player.getWorld().getEntities().stream()
                        .filter(ent -> ent instanceof Creature)
                        .map(ent -> (Creature) ent)
                        .forEach(ent -> stopTarget(ent, player));
            }
            // call post event
            PostPlayerHideEvent e2 = new PostPlayerHideEvent(player, silent);
            plugin.getServer().getPluginManager().callEvent(e2);
        } catch (Exception e) {
            plugin.logException(e);
        }
    }

    private void stopTarget(Creature mob, Player player) {
        Runnable runnable = () -> {
            if (mob.getTarget() != null && mob.getTarget().getUniqueId().equals(player.getUniqueId())) {
                mob.setTarget(null);
            }
        };
        if (FoliaChecker.isFolia()) {
            Scheduler.plugin(plugin).sync().runTask(runnable);
        } else {
            runnable.run();
        }
    }

    public void showPlayer(final Player player, final String showerName, boolean silent) {
        try {
            Validation.checkNotNull("player cannot be null", player);
            if (!plugin.getVanishStateMgr().isVanished(player.getUniqueId())) {
                plugin.log(Level.WARNING,
                        "Failed to show " + player.getName() + " since that player is already visible.");
                return;
            }
            // call event
            PlayerShowEvent e = new PlayerShowEvent(player, silent);
            plugin.getServer().getPluginManager().callEvent(e);
            if (e.isCancelled()) return;
            silent = e.isSilent();
            // metadata
            player.removeMetadata("vanished", plugin);
            // show
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                if (!plugin.hasPermissionToSee(onlinePlayer, player))
                    plugin.getVisibilityChanger().getHider().setHidden(player, onlinePlayer, false);
            // action bars
            if (plugin.getActionBarMgr() != null && config.getBoolean("MessageOptions.DisplayActionBar")) {
                plugin.getActionBarMgr().removeActionBar(player);
            }
            // sleep state
            player.setSleepingIgnored(false);
            // state
            plugin.getVanishStateMgr().setVanishedState(player.getUniqueId(),
                    player.getName(), false, showerName);
            // chat message
            if (showerName == null)
                plugin.sendMessage(player, "OnReappear", player);
            else
                plugin.sendMessage(player, "OnReappearCausedByOtherPlayer", player, showerName);
            // fly
            // remove fly if not spectator or creative and no perm
            if (config.getBoolean("InvisibilityFeatures.Fly.DisableOnReappear")
                    && !player.hasPermission("sv.keepfly")
                    && (player.getGameMode() == GameMode.SURVIVAL
                    || player.getGameMode() == GameMode.ADVENTURE)
                    && config.getBoolean("InvisibilityFeatures.Fly.Enable")) {
                player.setAllowFlight(false);
            }
            // call post event
            PostPlayerShowEvent e2 = new PostPlayerShowEvent(player, silent);
            plugin.getServer().getPluginManager().callEvent(e2);
        } catch (Exception e) {
            plugin.logException(e);
        }
    }
}