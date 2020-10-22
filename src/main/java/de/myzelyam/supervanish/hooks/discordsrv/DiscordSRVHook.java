package de.myzelyam.supervanish.hooks.discordsrv;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hooks.PluginHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DiscordSRVHook extends PluginHook {
	private final FakePlayerJoinLeaveListener fakeListener;

	public DiscordSRVHook(SuperVanish superVanish) {
		super(superVanish);
		fakeListener = new FakePlayerJoinLeaveListener();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onVanish(PlayerHideEvent e) {
		//Small hack to get DiscordSRV to fake a leave message.
		fakeListener.PlayerQuitEvent(new PlayerQuitEvent(e.getPlayer(), e.getPlayer().getName() + " left the game"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onReappear(PlayerShowEvent e) {
		//Small hack to get DiscordSRV to fake a join message.
		fakeListener.onPlayerJoin(new PlayerJoinEvent(e.getPlayer(), e.getPlayer().getName() + " joined the game"));
	}
}
