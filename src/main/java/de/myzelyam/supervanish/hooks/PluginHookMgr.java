/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class PluginHookMgr implements Listener {

    private static final Map<String, Class<? extends PluginHook>> REGISTERED_HOOKS
            = new HashMap<String, Class<? extends PluginHook>>() {{
        put("Essentials", EssentialsHook.class);
        put("Citizens", CitizensHook.class);
        put("PlaceholderAPI", PlaceholderAPIHook.class);
        put("dynmap", DynmapHook.class);
        put("TrailGUI", TrailGUIHook.class);
        put("MVdWPlaceholderAPI", MVdWPlaceholderAPIHook.class);
        put("OpenInv", OpenInvHook.class);
    }};
    private final SuperVanish plugin;
    private Set<PluginHook> activeHooks = new HashSet<>();

    public PluginHookMgr(SuperVanish plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Plugin alreadyEnabledPlugin : Bukkit.getPluginManager().getPlugins())
            if (alreadyEnabledPlugin.isEnabled())
                onPluginEnable(new PluginEnableEvent(alreadyEnabledPlugin));
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        Plugin plugin = e.getPlugin();
        if (REGISTERED_HOOKS.get(plugin.getName()) == null) return;
        if (isHookDisabled(plugin.getName())) return;
        PluginHook hook = null;
        try {
            hook = REGISTERED_HOOKS.get(plugin.getName()).getConstructor(SuperVanish.class).
                    newInstance(PluginHookMgr.this.plugin);
            hook.setPlugin(plugin);
            hook.onPluginEnable(plugin);
            Bukkit.getPluginManager().registerEvents(hook, plugin);
            activeHooks.add(hook);
            PluginHookMgr.this.plugin.log(
                    Level.INFO, "Hooked into " + plugin.getName());
        } catch (NoClassDefFoundError er) {
            Bukkit.getLogger().warning("NoClassDefFoundError for SV-Hook(v"
                    + this.plugin.getDescription().getVersion() + ") "
                    + (hook != null ? hook.getClass().getSimpleName() : "?") + " of plugin "
                    + plugin.getName() + " v" + plugin.getDescription().getVersion()
                    + ", please report this if you are using the latest version of that" +
                    " plugin!");
        } catch (Exception er) {
            if (er.getMessage() != null
                    && er.getMessage().contains("Unable to find handler list for event")) {
                this.plugin.log(Level.WARNING, "" + er.getMessage()
                        + "; This is not an issue with SuperVanish");
                return;
            } else if (er.getCause() != null && er.getCause().getMessage() != null && er.getCause()
                    .getMessage().contains("Unable to find handler list for event ")) {
                this.plugin.log(Level.WARNING, "" + er.getCause()
                        .getMessage() + "; This is not an issue with SuperVanish");
                return;
            }
            this.plugin.logException(new InvalidPluginHookException(er));
            this.plugin.log(Level.WARNING, "Affected by this error is only the "
                    + plugin.getName() + "-Hook, all other hooks and features aren't affected.");
        }
    }

    private boolean isHookDisabled(String pluginName) {
        if (pluginName.equalsIgnoreCase("dynmap"))
            return !plugin.getSettings().getBoolean("HookOptions.EnableDynmapHook");
        return !plugin.getSettings().getBoolean("HookOptions.Enable" + pluginName + "Hook", true);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        Plugin plugin = e.getPlugin();
        PluginHook hook = getActiveHook(plugin);
        if (hook == null) return;
        try {
            hook.onPluginDisable(plugin);
        } catch (Exception e1) {
            this.plugin.logException(new InvalidPluginHookException(e1));
        }
        hook.setPlugin(null);
        activeHooks.remove(hook);
    }

    private PluginHook getActiveHook(Plugin plugin) {
        for (PluginHook hook : activeHooks) if (hook.getPlugin() == plugin) return hook;
        return null;
    }

    public boolean isHookActive(Class<? extends PluginHook> hookClass) {
        for (PluginHook hook : activeHooks) {
            if (hook.getClass().equals(hookClass)) {
                return true;
            }
        }
        return false;
    }
}
