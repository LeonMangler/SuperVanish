/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import de.myzelyam.supervanish.visibility.VanishStateMgr;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface SuperVanishPlugin {

    void log(Level level, String msg);

    void log(Level level, String msg, Throwable ex);

    Logger getLogger();

    void logException(Throwable e);

    VanishStateMgr getVanishStateMgr();
}
