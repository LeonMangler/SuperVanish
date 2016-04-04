/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

/**
 * Represents the inventory of a DoubleChest
 * equals() and hashcode() is based of the two base inventories
 */
public class DoubleChestInventoryWrapper {

    private final Inventory leftInventory, rightInventory;

    public DoubleChestInventoryWrapper(Inventory leftInventory, Inventory rightInventory) {
        this.leftInventory = leftInventory;
        this.rightInventory = rightInventory;
    }

    public DoubleChestInventoryWrapper(DoubleChestInventory inventory) {
        this.leftInventory = inventory.getLeftSide();
        this.rightInventory = inventory.getRightSide();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DoubleChestInventoryWrapper that = (DoubleChestInventoryWrapper) obj;
        return (leftInventory.equals(that.leftInventory) && rightInventory.equals(that.rightInventory))
                || (leftInventory.equals(that.rightInventory) && rightInventory.equals(that.leftInventory));
    }

    @Override
    public int hashCode() {
        int inv1 = leftInventory != null ? leftInventory.hashCode() : 1;
        int inv2 = rightInventory != null ? rightInventory.hashCode() : 1;
        return inv1 * inv2 * 37;
    }
}
