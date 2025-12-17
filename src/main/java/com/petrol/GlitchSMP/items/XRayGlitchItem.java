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

public class XRayGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public XRayGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "glitch_id");
    }

    @Override
    public String getId() {
        return "xray";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "XRay Glitch";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                ChatColor.GRAY + "4m cooldown",
                "",
                ChatColor.WHITE + "Highlight nearby players and ores",
                ChatColor.WHITE + "with a glowing effect for 20s."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 28;
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
