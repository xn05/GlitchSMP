package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DreamGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public DreamGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "dream_glitch");
    }

    @Override
    public String getId() {
        return "dream";
    }

    @Override
    public String getDisplayName() {
        return "§aDream Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§75m Cooldown",
                "",
                "§fWhen active, mobs drop 20x loot",
                "§ffor 2 minutes. Mobs holding shulkers",
                "§fwill not drop multiple shulkers."
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
        return "∛";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
