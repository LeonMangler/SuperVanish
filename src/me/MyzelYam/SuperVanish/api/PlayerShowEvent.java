package me.MyzelYam.SuperVanish.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Deprecated
public class PlayerShowEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean isCancelled = false;

    private Player p = null;

    public PlayerShowEvent(Player p) {
        this.p = p;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @return The player
     */
    public Player getPlayer() {
        return p;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
