package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class HorsetamerGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public HorsetamerGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "horsetamer_glitch");
    }

    @Override
    public String getId() {
        return "horsetamer";
    }

    @Override
    public String getDisplayName() {
        return "§6Horsetamer Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§710m Cooldown",
                "",
                "§fSummons a skeleton horse with a saddle",
                "§fat your location."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 11;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
