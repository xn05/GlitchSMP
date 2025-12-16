package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class BedrockGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public BedrockGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "bedrock_glitch");
    }

    @Override
    public String getId() {
        return "bedrock";
    }

    @Override
    public String getDisplayName() {
        return "§fBedrock Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72m Cooldown",
                "",
                "§fFor 15 seconds, all blocks you place",
                "§fbecome unbreakable."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 14;
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
