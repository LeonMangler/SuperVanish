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

import ca.spottedleaf.concurrentutil.map.SWMRHashTable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PluginHookMgr implements Listener {

    private static final Map<String, Class<? extends PluginHook>> REGISTERED_HOOKS;
    static {
        Map<String, Class<? extends PluginHook>> m = new SWMRHashTable<>();
        m.put("Essentials", EssentialsHook.class);
        m.put("Citizens", CitizensHook.class);
        m.put("PlaceholderAPI", PlaceholderAPIHook.class);
        m.put("dynmap", DynmapHook.class);
        m.put("TrailGUI", TrailGUIHook.class);
        m.put("MVdWPlaceholderAPI", MVdWPlaceholderAPIHook.class);
        m.put("OpenInv", OpenInvHook.class);
        m.put("InvSeePlusPlus", InvseePlusPlusHook.class);
        REGISTERED_HOOKS = java.util.Collections.unmodifiableMap(m);
    }
    private final SuperVanish plugin;
    private final Map<Plugin, PluginHook> activeHooks = new ConcurrentHashMap<>();

    public PluginHookMgr(SuperVanish plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Plugin alreadyEnabledPlugin : Bukkit.getPluginManager().getPlugins())
            if (alreadyEnabledPlugin.isEnabled())
                onPluginEnable(new PluginEnableEvent(alreadyEnabledPlugin));
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        Plugin plugin = e.getPlugin();
        if (REGISTERED_HOOKS.get(plugin.getName()) == null) return;
        if (isHookDisabled(plugin.getName())) return;
        PluginHook hook = null;
        try {
            synchronized (activeHooks) {
                if (activeHooks.containsKey(plugin)) return;

            hook = REGISTERED_HOOKS.get(plugin.getName()).getConstructor(SuperVanish.class).
                    newInstance(PluginHookMgr.this.plugin);
            hook.setPlugin(plugin);
            hook.onPluginEnable(plugin);
            Bukkit.getPluginManager().registerEvents(hook, PluginHookMgr.this.plugin);
                activeHooks.put(plugin, hook);
            }
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
                this.plugin.log(Level.WARNING, er.getMessage()
                        + "; This is not an issue with SuperVanish");
                return;
            } else if (er.getCause() != null && er.getCause().getMessage() != null && er.getCause()
                    .getMessage().contains("Unable to find handler list for event ")) {
                this.plugin.log(Level.WARNING, er.getCause()
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
        PluginHook hook;
        synchronized (activeHooks) {
            hook = activeHooks.remove(plugin);
        }
        if (hook == null) return;
        try {
            hook.onPluginDisable(plugin);
        } catch (Exception e1) {
            this.plugin.logException(new InvalidPluginHookException(e1));
        }
        org.bukkit.event.HandlerList.unregisterAll(hook);
        hook.setPlugin(null);
    }

    private PluginHook getActiveHook(Plugin plugin) {
        return activeHooks.get(plugin);
    }

    public boolean isHookActive(Class<? extends PluginHook> hookClass) {
        Collection<PluginHook> hooks = activeHooks.values();
        for (PluginHook hook : hooks) {
            if (hook.getClass().equals(hookClass)) {
                return true;
            }
        }
        return false;
    }
}
