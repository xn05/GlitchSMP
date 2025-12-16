package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class RedstoneGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public RedstoneGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "redstone_glitch");
    }

    @Override
    public String getId() {
        return "redstone";
    }

    @Override
    public String getDisplayName() {
        return "§cRedstone Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72.5m Cooldown",
                "",
                "§fFor each redstone block in your inventory,",
                "§fyou gain 1 redstone point. When activated,",
                "§fyour total damage dealt is increased by",
                "§fyour total redstone points divided by 100."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 4;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}

