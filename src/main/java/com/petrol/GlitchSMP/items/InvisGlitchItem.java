package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class InvisGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public InvisGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "invis_glitch");
    }

    @Override
    public String getId() {
        return "invis";
    }

    @Override
    public String getDisplayName() {
        return "§7Invis Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§74m Cooldown",
                "",
                "§fMakes you invisible for 30 seconds."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 26;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
