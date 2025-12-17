package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class GravityGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public GravityGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "gravity_glitch");
    }

    @Override
    public String getId() {
        return "gravity";
    }

    @Override
    public String getDisplayName() {
        return "§9Gravity Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§71m Cooldown",
                "",
                "§fFor 15s players within a 7 block radius",
                "§fwill have low gravity."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 10;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
