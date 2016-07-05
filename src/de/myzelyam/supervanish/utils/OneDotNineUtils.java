/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import org.bukkit.scoreboard.Team;

public class OneDotNineUtils {

    public static void setNoPushForTeam(Team t) {
        t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
}
