/*
 * DiscordSRV - A Minecraft to Discord and back link plugin
 * Copyright (C) 2016-2020 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.myzelyam.supervanish.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.commons.lang3.StringUtils;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.MessageFormat;
import github.scarsz.discordsrv.objects.managers.GroupSynchronizationManager;
import github.scarsz.discordsrv.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.BiFunction;

public class FakePlayerJoinLeaveListener {
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		// if player is OP & update is available tell them
		if (GamePermissionUtil.hasPermission(player, "discordsrv.updatenotification") && DiscordSRV.updateIsAvailable) {
			event.getPlayer().sendMessage(DiscordSRV.getPlugin().getDescription().getVersion().endsWith("-SNAPSHOT")
					? ChatColor.GRAY + "There is a newer development build of DiscordSRV available. Download it at https://snapshot.discordsrv.com/"
					: ChatColor.AQUA + "An update to DiscordSRV is available. Download it at https://www.spigotmc.org/resources/discordsrv.18494/ or https://get.discordsrv.com"
			);
		}

		if (DiscordSRV.getPlugin().isGroupRoleSynchronizationEnabled()) {
			// trigger a synchronization for the player
			DiscordSRV.getPlugin().getGroupSynchronizationManager().resync(player, GroupSynchronizationManager.SyncCause.PLAYER_JOIN);
		}

		if (PlayerUtil.isVanished(player)) {
			DiscordSRV.debug("Not sending a join message for " + event.getPlayer().getName() + " because a vanish plugin reported them as vanished");
			return;
		}

		MessageFormat messageFormat = event.getPlayer().hasPlayedBefore()
				? DiscordSRV.getPlugin().getMessageFromConfiguration("MinecraftPlayerJoinMessage")
				: DiscordSRV.getPlugin().getMessageFromConfiguration("MinecraftPlayerFirstJoinMessage");

		// make sure join messages enabled
		if (messageFormat == null) return;

		final String name = player.getName();

		// check if player has permission to not have join messages
		if (GamePermissionUtil.hasPermission(event.getPlayer(), "discordsrv.silentjoin")) {
			DiscordSRV.info(LangUtil.InternalMessage.SILENT_JOIN.toString()
					.replace("{player}", name)
			);
			return;
		}

		// player doesn't have silent join permission, send join message

		// schedule command to run in a second to be able to capture display name
		Bukkit.getScheduler().runTaskLater(DiscordSRV.getPlugin(), () -> {
			TextChannel textChannel = DiscordSRV.getPlugin().getOptionalTextChannel("join");
			if (textChannel == null) {
				DiscordSRV.debug("Not sending join message, text channel is null");
				return;
			}

			final String displayName = StringUtils.isNotBlank(player.getDisplayName()) ? DiscordUtil.strip(player.getDisplayName()) : "";
			final String message = StringUtils.isNotBlank(event.getJoinMessage()) ? event.getJoinMessage() : "";
			final String avatarUrl = DiscordSRV.getPlugin().getEmbedAvatarUrl(player);
			final String botAvatarUrl = DiscordUtil.getJda().getSelfUser().getEffectiveAvatarUrl();
			String botName = DiscordSRV.getPlugin().getMainGuild() != null ? DiscordSRV.getPlugin().getMainGuild().getSelfMember().getEffectiveName() : DiscordUtil.getJda().getSelfUser().getName();

			BiFunction<String, Boolean, String> translator = (content, needsEscape) -> {
				if (content == null) return null;
				content = content
						.replaceAll("%time%|%date%", TimeUtil.timeStamp())
						.replace("%message%", DiscordUtil.strip(needsEscape ? DiscordUtil.escapeMarkdown(message) : message))
						.replace("%username%", needsEscape ? DiscordUtil.escapeMarkdown(name) : name)
						.replace("%displayname%", needsEscape ? DiscordUtil.escapeMarkdown(displayName) : displayName)
						.replace("%usernamenoescapes%", name)
						.replace("%displaynamenoescapes%", displayName)
						.replace("%embedavatarurl%", avatarUrl)
						.replace("%botavatarurl%", botAvatarUrl)
						.replace("%botname%", botName);
				content = DiscordUtil.translateEmotes(content, textChannel.getGuild());
				content = PlaceholderUtil.replacePlaceholdersToDiscord(content, player);
				return content;
			};

			Message discordMessage = DiscordSRV.getPlugin().translateMessage(messageFormat, translator);
			if (discordMessage == null) return;

			String webhookName = translator.apply(messageFormat.getWebhookName(), false);
			String webhookAvatarUrl = translator.apply(messageFormat.getWebhookAvatarUrl(), false);

			if (messageFormat.isUseWebhooks()) {
				WebhookUtil.deliverMessage(textChannel, webhookName, webhookAvatarUrl,
						discordMessage.getContentRaw(), discordMessage.getEmbeds().stream().findFirst().orElse(null));
			} else {
				DiscordUtil.queueMessage(textChannel, discordMessage);
			}
		}, 20);

		// if enabled, set the player's discord nickname as their ign
		if (DiscordSRV.config().getBoolean("NicknameSynchronizationEnabled")) {
			final String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
			DiscordSRV.getPlugin().getNicknameUpdater().setNickname(DiscordUtil.getMemberById(discordId), player);
		}
	}

	public void PlayerQuitEvent(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		if (PlayerUtil.isVanished(player)) {
			DiscordSRV.debug("Not sending a quit message for " + event.getPlayer().getName() + " because a vanish plugin reported them as vanished");
			return;
		}

		MessageFormat messageFormat = DiscordSRV.getPlugin().getMessageFromConfiguration("MinecraftPlayerLeaveMessage");

		// make sure quit messages enabled
		if (messageFormat == null) return;

		final String name = player.getName();

		// no quit message, user shouldn't have one from permission
		if (GamePermissionUtil.hasPermission(event.getPlayer(), "discordsrv.silentquit")) {
			DiscordSRV.info(LangUtil.InternalMessage.SILENT_QUIT.toString()
					.replace("{player}", name)
			);
			return;
		}

		TextChannel textChannel = DiscordSRV.getPlugin().getOptionalTextChannel("leave");
		if (textChannel == null) {
			DiscordSRV.debug("Not sending quit message, text channel is null");
			return;
		}

		final String displayName = StringUtils.isNotBlank(player.getDisplayName()) ? DiscordUtil.strip(player.getDisplayName()) : "";
		final String message = StringUtils.isNotBlank(event.getQuitMessage()) ? event.getQuitMessage() : "";

		String avatarUrl = DiscordSRV.getPlugin().getEmbedAvatarUrl(event.getPlayer());
		String botAvatarUrl = DiscordUtil.getJda().getSelfUser().getEffectiveAvatarUrl();
		String botName = DiscordSRV.getPlugin().getMainGuild() != null ? DiscordSRV.getPlugin().getMainGuild().getSelfMember().getEffectiveName() : DiscordUtil.getJda().getSelfUser().getName();

		BiFunction<String, Boolean, String> translator = (content, needsEscape) -> {
			if (content == null) return null;
			content = content
					.replaceAll("%time%|%date%", TimeUtil.timeStamp())
					.replace("%message%", DiscordUtil.strip(needsEscape ? DiscordUtil.escapeMarkdown(message) : message))
					.replace("%username%", DiscordUtil.strip(needsEscape ? DiscordUtil.escapeMarkdown(name) : name))
					.replace("%displayname%", needsEscape ? DiscordUtil.escapeMarkdown(displayName) : displayName)
					.replace("%usernamenoescapes%", name)
					.replace("%displaynamenoescapes%", displayName)
					.replace("%embedavatarurl%", avatarUrl)
					.replace("%botavatarurl%", botAvatarUrl)
					.replace("%botname%", botName);
			content = DiscordUtil.translateEmotes(content, textChannel.getGuild());
			content = PlaceholderUtil.replacePlaceholdersToDiscord(content, player);
			return content;
		};

		Message discordMessage = DiscordSRV.getPlugin().translateMessage(messageFormat, translator);
		if (discordMessage == null) return;

		String webhookName = translator.apply(messageFormat.getWebhookName(), false);
		String webhookAvatarUrl = translator.apply(messageFormat.getWebhookAvatarUrl(), false);

		// player doesn't have silent quit, show quit message
		if (messageFormat.isUseWebhooks()) {
			WebhookUtil.deliverMessage(textChannel, webhookName, webhookAvatarUrl,
					discordMessage.getContentRaw(), discordMessage.getEmbeds().stream().findFirst().orElse(null));
		} else {
			DiscordUtil.queueMessage(textChannel, discordMessage);
		}
	}
}
