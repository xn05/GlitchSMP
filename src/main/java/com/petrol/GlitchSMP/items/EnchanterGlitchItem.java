package com.petrol.GlitchSMP.items;

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

public class EnchanterGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public EnchanterGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "glitch_id");
    }

    @Override
    public String getId() {
        return "enchanter";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Enchanter Glitch";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                ChatColor.GRAY + "3m Cooldown",
                "",
                ChatColor.WHITE + "Temporarily boost tool enchantments",
                ChatColor.WHITE + "by 1 level for 30s."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 5;
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
