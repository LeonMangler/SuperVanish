/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands.subcommands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;
import de.myzelyam.supervanish.commands.SubCommand;
import de.myzelyam.supervanish.utils.FoliaUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VanishOther extends SubCommand {

    private Player specifiedPlayer;

    public VanishOther(SuperVanish plugin) {
        this(null, plugin);
    }

    public VanishOther(Player specifiedPlayer, SuperVanish plugin) {
        super(plugin);
        this.specifiedPlayer = specifiedPlayer;
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.VANISH_OTHER, true)) {
            boolean hide = false, offline = false, silent = false;
            Player target;
            String name;
            UUID uuid;
            UUID senderUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
            String senderName = sender.getName();
            if (specifiedPlayer == null) {
                if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")
                        || args[0].equalsIgnoreCase("vanish"))
                    hide = true;
                target = Bukkit.getPlayer(args[1]);
                name = target == null || !FoliaUtil.isOwnedByCurrentRegion(target) ? args[1] : target.getName();
            } else {
                target = specifiedPlayer;
                if (!FoliaUtil.isOwnedByCurrentRegion(target)) {
                    boolean finalSilent = args.length == 3 && args[2].equalsIgnoreCase("-s")
                            || args.length == 2 && args[1].equalsIgnoreCase("-s");
                    FoliaUtil.runAtEntity(plugin, target, () -> executeOnlineTarget(sender, senderName, senderUuid,
                            target, !isVanished(target.getUniqueId()), finalSilent));
                    return;
                }
                name = target.getName();
                hide = !isVanished(target.getUniqueId());
            }
            if (target != null && !FoliaUtil.isOwnedByCurrentRegion(target)) {
                boolean finalHide = hide;
                boolean finalSilent = args.length == 3 && args[2].equalsIgnoreCase("-s")
                        || args.length == 2 && args[1].equalsIgnoreCase("-s");
                FoliaUtil.runAtEntity(plugin, target,
                        () -> executeOnlineTarget(sender, senderName, senderUuid, target, finalHide, finalSilent));
                return;
            }
            if (target == null) {
                offline = true;
                uuid = plugin.getVanishStateMgr().getVanishedUUIDFromNameOnFile(name);
                if (uuid == null) {
                    plugin.sendMessage(sender, "PlayerNonExistent", sender, name);
                    return;
                }
            } else {
                name = target.getName();
                uuid = target.getUniqueId();
            }
            if (!offline && sender instanceof Player && sender != target
                    && target.hasPermission("sv.notoggle")) {
                plugin.sendMessage(sender, "CannotHideOtherPlayer", sender, name);
                return;
            }
            if (plugin.getSettings().getBoolean(
                    "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false) && target != null
                    && senderUuid != null && plugin.getVisibilityChanger().getHider()
                    .isHidden(target.getUniqueId(), senderUuid)) {
                plugin.sendMessage(sender, "PlayerNonExistent", sender, name);
                return;
            }
            if (hide && (offline ? isVanished(uuid) : isVanished(uuid))) {
                plugin.sendMessage(sender, "AlreadyInvisibleMessage", sender, name);
                return;
            } else if (!hide && !(offline ? isVanished(uuid) : isVanished(uuid))) {
                plugin.sendMessage(sender, "AlreadyVisibleMessage", sender, name);
                return;
            }
            if (args.length == 3)
                silent = args[2].equalsIgnoreCase("-s");
            else if (args.length == 2)
                silent = args[1].equalsIgnoreCase("-s");
            if (!offline) {
                if (hide) {
                    plugin.getVisibilityChanger().hidePlayer(target, sender.getName(), silent);
                    plugin.sendMessage(sender, "HideOtherMessage", sender, name);
                } else {
                    plugin.getVisibilityChanger().showPlayer(target, sender.getName());
                    plugin.sendMessage(sender, "ShowOtherMessage", sender, name, silent);
                }
            } else {
                if (hide) {
                    plugin.getVanishStateMgr().setVanishedState(uuid, name, true, sender.getName());
                    plugin.sendMessage(sender, "HideOtherMessage", sender, name);
                } else {
                    plugin.getVanishStateMgr().setVanishedState(uuid, name, false, sender.getName());
                    plugin.sendMessage(sender, "ShowOtherMessage", sender, name);
                }
            }
        }
    }

    private void executeOnlineTarget(CommandSender sender, String senderName, UUID senderUuid, Player target,
                                     boolean hide, boolean silent) {
        String name = target.getName();
        UUID uuid = target.getUniqueId();
        if (senderUuid != null && !senderUuid.equals(uuid) && target.hasPermission("sv.notoggle")) {
            plugin.sendMessage(sender, "CannotHideOtherPlayer", sender, name);
            return;
        }
        if (plugin.getSettings().getBoolean(
                "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false)
                && senderUuid != null && plugin.getVisibilityChanger().getHider().isHidden(uuid, senderUuid)) {
            plugin.sendMessage(sender, "PlayerNonExistent", sender, name);
            return;
        }
        if (hide && isVanished(uuid)) {
            plugin.sendMessage(sender, "AlreadyInvisibleMessage", sender, name);
            return;
        } else if (!hide && !isVanished(uuid)) {
            plugin.sendMessage(sender, "AlreadyVisibleMessage", sender, name);
            return;
        }
        if (hide) {
            plugin.getVisibilityChanger().hidePlayer(target, senderName, silent);
            plugin.sendMessage(sender, "HideOtherMessage", sender, name);
        } else {
            plugin.getVisibilityChanger().showPlayer(target, senderName);
            plugin.sendMessage(sender, "ShowOtherMessage", sender, name, silent);
        }
    }
}
