package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class FreezeGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public FreezeGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "freeze_glitch");
    }

    @Override
    public String getId() {
        return "freeze";
    }

    @Override
    public String getDisplayName() {
        return "§bFreeze Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§71.5m Cooldown",
                "",
                "§fUpon activation, your next hit within",
                "§f15 seconds will freeze the target for",
                "§f4 seconds, preventing movement and item use."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 2;
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
