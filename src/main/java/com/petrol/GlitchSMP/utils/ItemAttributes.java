package com.petrol.GlitchSMP.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public interface ItemAttributes {
    String getId();

    String getDisplayName();

    List<String> getLore();

    Material getMaterial();

    int getCustomModelData();

    /**
     * Glyph used by the actionbar/resource-pack UI. Return null for fallback text.
     */
    default String getGlyph() {
        return null;
    }

    /**
     * Optional persistent data key for item identification.
     */
    NamespacedKey getKey();

    /**
     * Called while the glitch is still an item (prior to equipping).
     */
    default void onRightClick(PlayerInteractEvent event) {
    }

    default void onLeftClick(PlayerInteractEvent event) {
    }

    /**
     * Convenience for building the display item.
     */
    default ItemStack createItemStack() {
        ItemStack stack = new ItemStack(getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + getDisplayName());
            meta.setLore(Collections.unmodifiableList(getLore()));
            meta.setCustomModelData(getCustomModelData());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
