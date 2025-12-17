package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class FakeBlockGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public FakeBlockGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "fake_block_glitch");
    }

    @Override
    public String getId() {
        return "fake_block";
    }

    @Override
    public String getDisplayName() {
        return "§aFake Block Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72m Cooldown",
                "",
                "§fPlace a fake block of the item you're holding",
                "§fat your location. Activate again to remove",
                "§fit and place a new one elsewhere."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 3;
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
