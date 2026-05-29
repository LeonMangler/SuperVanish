/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActionBarMgr {

    private final SuperVanish plugin;
    private final List<Player> actionBars = new CopyOnWriteArrayList<>();

    public ActionBarMgr(SuperVanish plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        FoliaUtil.runGlobalTimer(plugin, () -> {
                for (Player p : actionBars) {
                    FoliaUtil.runAtEntity(plugin, p, () -> {
                        if (!p.isOnline()) {
                            actionBars.remove(p);
                            return;
                        }
                        try {
                            sendActionBar(p, plugin.replacePlaceholders(plugin.getMessage("ActionBarMessage"), p));
                        } catch (Exception | LinkageError e) {
                            plugin.logException(e);
                            plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                                    "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                                    "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                                    "of an optional feature module and can be removed safely by disabling " +
                                    "DisplayActionBar in the config file. Please report this " +
                                    "error if you can reproduce it on an up-to-date server with only latest " +
                                    "ProtocolLib and latest SV installed.");
                        }
                    });
                }
                return true;
        }, 0, 2 * 20);
    }

    private void sendActionBar(Player p, String bar) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar));
    }

    public void addActionBar(Player p) {
        if (!actionBars.contains(p)) actionBars.add(p);
    }

    public void removeActionBar(Player p) {
        actionBars.remove(p);
    }
}
