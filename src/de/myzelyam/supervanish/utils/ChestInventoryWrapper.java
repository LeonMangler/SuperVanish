/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

/**
 * Represents the inventory of a Chest
 * equals() and hashcode() is based of the base inventory/inventories
 */
public class ChestInventoryWrapper {

    private final boolean isDoubleChest;

    private Inventory singleChestInventory;

    private DoubleChestInventoryWrapper doubleChestInventory;

    public ChestInventoryWrapper(Chest chest) {
        isDoubleChest = false;
        singleChestInventory = chest.getInventory();
    }

    public ChestInventoryWrapper(Chest chest1, Chest chest2) {
        isDoubleChest = true;
        this.doubleChestInventory
                = new DoubleChestInventoryWrapper(chest1.getInventory(), chest2.getInventory());
    }

    public ChestInventoryWrapper(Inventory inventory) {
        if (inventory instanceof DoubleChestInventory) {
            DoubleChestInventory doubleChestInventory = (DoubleChestInventory) inventory;
            isDoubleChest = true;
            this.doubleChestInventory = new DoubleChestInventoryWrapper(doubleChestInventory);
        } else {
            isDoubleChest = false;
            singleChestInventory = inventory;
        }
    }

    public ChestInventoryWrapper(Inventory inventory1, Inventory inventory2) {
        isDoubleChest = true;
        this.doubleChestInventory = new DoubleChestInventoryWrapper(inventory1, inventory2);
    }

    public boolean isDoubleChest() {
        return isDoubleChest;
    }

    public DoubleChestInventoryWrapper getDoubleChestInventoryWrapper() {
        if (!isDoubleChest()) throw new IllegalStateException("Inventory doesn't belong to a double chest");
        return doubleChestInventory;
    }

    @Override
    public boolean equals(Object obj) {
        // System.out.println("Comparing " + this + " with " + obj);
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChestInventoryWrapper that = (ChestInventoryWrapper) obj;
        return isDoubleChest()
                ? doubleChestInventory.equals(that.doubleChestInventory)
                : singleChestInventory.equals(that.singleChestInventory);
    }

    @Override
    public int hashCode() {
        return isDoubleChest() ? doubleChestInventory.hashCode() : singleChestInventory.hashCode();
    }
}
