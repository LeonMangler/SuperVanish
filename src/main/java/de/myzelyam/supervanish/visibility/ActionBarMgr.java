/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import com.google.common.collect.ImmutableList;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ActionBarMgr {

    private final List<Player> actionBars = new ArrayList<>();

    public ActionBarMgr(SuperVanish plugin) {
        startTimerTask(plugin);
    }

    private void startTimerTask(final SuperVanish plugin) {
        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    List<Player> actionBars = ImmutableList.copyOf(ActionBarMgr.this.actionBars);
                    for (Player player : actionBars) {
                        plugin.getProtocolLibPacketUtils().sendActionBar(
                                player, plugin.convertString(plugin.getMsg("ActionBarMessage"), player));
                    }
                } catch (Exception e) {
                    plugin.printException(e);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 2 * 20);
    }

    public void addActionBar(Player p) {
        actionBars.add(p);
    }

    public void removeActionBar(Player p) {
        actionBars.remove(p);
    }
}
