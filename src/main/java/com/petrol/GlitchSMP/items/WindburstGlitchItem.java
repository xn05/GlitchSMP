package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WindburstGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public WindburstGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "windburst_glitch");
    }

    @Override
    public String getId() {
        return "windburst";
    }

    @Override
    public String getDisplayName() {
        return "§fWindburst Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§730s Cooldown",
                "",
                "§fLaunches you forward 30 blocks",
                "§fwith wind particles."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 7;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
