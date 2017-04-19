/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

public class EnjinMinecraftPluginHook {

    public EnjinMinecraftPluginHook(final SuperVanish plugin) {
        /* Enjin.getApi().registerVanishPredicate(new Predicate<UUID>() {
            @Override
            public boolean apply(@Nullable UUID uuid) {
                if (uuid == null) return false;
                Player player = Bukkit.getPlayer(uuid);
                return player != null && VanishAPI.isInvisible(player);
            }
        }); */ // dismissing, api cannot be found.
    }
}
