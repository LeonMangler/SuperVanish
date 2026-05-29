/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * license, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility.hiders.modules;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.OneTimeWarning;
import de.myzelyam.supervanish.visibility.hiders.PlayerHider;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class TabCompleteModule extends PacketAdapter {
    private final PlayerHider hider;
    private final SuperVanish plugin;

    private final OneTimeWarning protocolWarning;
    private final AtomicBoolean fieldAccessWarningLogged = new AtomicBoolean(false);

    public TabCompleteModule(SuperVanish plugin, PlayerHider hider) {
        super(plugin, ListenerPriority.HIGH, PacketType.Play.Server.TAB_COMPLETE);
        this.plugin = plugin;
        this.hider = hider;
        protocolWarning = new OneTimeWarning(plugin, "IMPORTANT: Please make sure that you are using the latest " +
                "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                "of an optional invisibility module and can be removed safely by disabling " +
                "ModifyTabCompletePackets in the config. Please report this " +
                "error if you can reproduce it on an up-to-date server with only latest " +
                "ProtocolLib and latest SV installed.");
    }

    public static void register(SuperVanish plugin, PlayerHider hider) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new TabCompleteModule(plugin, hider));
    }


    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            try {
                Suggestions suggestions = event.getPacket().getSpecificModifier(Suggestions.class).read(0);
                Iterator<Suggestion> iterator = suggestions.getList().iterator();
                boolean containsHiddenPlayer = false;
                while (iterator.hasNext()) {
                    Suggestion suggestion = iterator.next();
                    String completion = suggestion.getText();
                    if (completion.contains("/")) continue;
                    if (hider.isHidden(completion, event.getPlayer())) {
                        iterator.remove();
                        containsHiddenPlayer = true;
                    }
                }
                if (containsHiddenPlayer) {
                    event.getPacket().getSpecificModifier(Suggestions.class).write(0, suggestions);
                }
            } catch (FieldAccessException e) {
                if (fieldAccessWarningLogged.compareAndSet(false, true)) {
                    plugin.getLogger().warning("Could not intercept tab-completions using ProtocolLib: "
                            + e.getMessage());
                }
            }
        } catch (Exception | LinkageError e) {
            if (e.getMessage() == null
                    || !e.getMessage().endsWith("is not supported for temporary players.")) {
                protocolWarning.log(e);
            }
        }
    }
}
