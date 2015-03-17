package me.MyzelYam.SuperVanish.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerHideEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean isCancelled = false;

	private Player p = null;

	public PlayerHideEvent(Player p) {
		this.p = p;
	}

	/**
	 * @return The player
	 */
	public Player getPlayer() {
		return p;
	}

	public boolean isCancelled() {
		return this.isCancelled;
	}

	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
