package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VirusGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public VirusGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "virus_glitch");
    }

    @Override
    public String getId() {
        return "virus";
    }

    @Override
    public String getDisplayName() {
        return "§aVirus Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72m Cooldown",
                "",
                "§fFor 15 seconds, the next player you hit",
                "§fwill be flashed randomly with a title",
                "§ffor 5 seconds."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 6;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
