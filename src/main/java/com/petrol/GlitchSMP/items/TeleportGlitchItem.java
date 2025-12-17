package com.petrol.GlitchSMP.items;

import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TeleportGlitchItem implements ItemAttributes {
    private final Plugin plugin;
    private final NamespacedKey key;

    public TeleportGlitchItem(Plugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "teleport_glitch");
    }

    @Override
    public String getId() {
        return "teleport";
    }

    @Override
    public String getDisplayName() {
        return "§2Teleport Glitch";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§72m Cooldown",
                "",
                "§fTeleport to the block you are looking at,",
                "§fup to 50 blocks away."
        );
    }

    @Override
    public Material getMaterial() {
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 15;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
