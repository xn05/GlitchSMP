package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ImmortalityGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public ImmortalityGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "immortality_glitch");
    }

    @Override
    public String getId() {
        return "immortality";
    }

    @Override
    public String getDisplayName() {
        return "§6Immortality Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§75m Cooldown",
                "",
                "§fBecome immortal for 30 seconds,",
                "§ftaking no damage from any source."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 19;
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
