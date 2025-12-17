package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class ShulkerGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public ShulkerGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "glitch_id");
    }

    @Override
    public String getId() {
        return "shulker";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_PURPLE + "Shulker Glitch";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                ChatColor.GRAY + "4m cooldown",
                ChatColor.DARK_GRAY + "",
                ChatColor.WHITE + "Opens a menu that lets you access an",
                ChatColor.WHITE + "anvil, crafting table and your ender chest."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 27;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(getMaterial());
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getDisplayName());
            meta.setLore(getLore());
            meta.setCustomModelData(getCustomModelData());
            meta.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING, getId());
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
