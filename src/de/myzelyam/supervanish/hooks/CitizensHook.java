/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Iterator;

public class CitizensHook implements Listener {

    private final SuperVanish plugin;

    public CitizensHook(SuperVanish plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNPCTarget(NPCSpeechEvent e) {
        e.setCancelled(!checkContext(e.getContext()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpeechTargeted(SpeechEvent e) {
        e.setCancelled(!checkContext(e.getContext()));
    }

    /**
     * @return FALSE if the event should be cancelled, TRUE otherwise
     */
    private boolean checkContext(SpeechContext context) {
        // don't let it talk about hidden players
        for (Player p : plugin.getOnlineInvisiblePlayers()) {
            if (context.getMessage().contains(p.getName())) {
                return false;
            }
        }
        // don't let hidden players receive it
        Iterator<Talkable> iterator = context.iterator();
        while (iterator.hasNext()) {
            Talkable recipient = iterator.next();
            Entity entity = recipient.getEntity();
            if (!(entity instanceof Player))
                continue;
            Player player = (Player) entity;
            if (VanishAPI.isInvisible(player)) {
                iterator.remove();
            }
        }
        // cancel contexts with only hidden recipients
        return context.hasRecipients();
    }
}
