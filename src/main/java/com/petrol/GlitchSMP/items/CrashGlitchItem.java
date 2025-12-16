package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CrashGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public CrashGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "crash_glitch");
    }

    @Override
    public String getId() {
        return "crash";
    }

    @Override
    public String getDisplayName() {
        return "§dCrash Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72m Cooldown",
                "",
                "§fWhen activated, your next hit within",
                "§f15 seconds will crash the target's game,",
                "§fbanning them for 15 seconds."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 25;
    }

    @Override
    public String getGlyph() {
        return "∜";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
