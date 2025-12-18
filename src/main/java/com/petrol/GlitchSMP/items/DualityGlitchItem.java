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

public class DualityGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public DualityGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "glitch_id");
    }

    @Override
    public String getId() {
        return "duality";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.DARK_RED + "Duality " + ChatColor.DARK_BLUE + "Glitch";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                ChatColor.GRAY + "2m cooldown",
                "",
                ChatColor.WHITE + "For 15s: Hits freeze enemies for 2s,",
                ChatColor.WHITE + "burn nearby enemies within 2 blocks. "
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 29;
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
