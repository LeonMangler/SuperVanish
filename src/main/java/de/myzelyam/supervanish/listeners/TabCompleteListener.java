/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * license, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.listeners;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Iterator;
import java.util.Objects;

public class TabCompleteListener implements Listener {

    private final SuperVanish plugin;

    public TabCompleteListener(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent e) {
        try {
            if (!(e.getSender() instanceof Player)) return;
            Player p = (Player) e.getSender();
            Iterator<String> it = e.getCompletions().iterator();
            while (it.hasNext()) {
                String completion = it.next();
                boolean allowedCompletion = plugin.getVanishStateMgr().getOnlineVanishedPlayers().stream()
                        .map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .filter(vanishedPlayer -> !plugin.canSee(p, vanishedPlayer))
                        .map(HumanEntity::getName)
                        .noneMatch(name -> name.equalsIgnoreCase(completion));
                if (!allowedCompletion) {
                    it.remove();
                }
            }

        } catch (Exception er) {
            plugin.logException(er);
        }
    }

}
