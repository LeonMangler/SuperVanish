/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import com.google.common.collect.ImmutableList;

import de.myzelyam.supervanish.SuperVanish;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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
                        String message = plugin.convertString(plugin.getMsg("ActionBarMessage"), player);
                        if (!plugin.isOneDotXOrHigher(11)) {
                            plugin.getProtocolLibPacketUtils().sendActionBarLegacy(player, message);
                        } else
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent
                                    .fromLegacyText(message));
                    }
                } catch (Exception e) {
                    plugin.printException(e);
                    cancel();
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
