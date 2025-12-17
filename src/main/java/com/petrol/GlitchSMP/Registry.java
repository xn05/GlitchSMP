package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.ItemAttributes;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Registry {
    private void registerGlitches() {
        registerGlitch("crash",
                new com.petrol.GlitchSMP.items.CrashGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.CrashGlitchAbility());
        registerGlitch("redstone",
                new com.petrol.GlitchSMP.items.RedstoneGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.RedstoneGlitchAbility());
        registerGlitch("dream",
                new com.petrol.GlitchSMP.items.DreamGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.DreamGlitchAbility());
        registerGlitch("freeze",
                new com.petrol.GlitchSMP.items.FreezeGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.FreezeGlitchAbility());
        registerGlitch("bedrock",
                new com.petrol.GlitchSMP.items.BedrockGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.BedrockGlitchAbility());
        registerGlitch("immortality",
                new com.petrol.GlitchSMP.items.ImmortalityGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.ImmortalityGlitchAbility());
        registerGlitch("fake_block",
                new com.petrol.GlitchSMP.items.FakeBlockGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.FakeBlockGlitchAbility());
        registerGlitch("teleport",
                new com.petrol.GlitchSMP.items.TeleportGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.TeleportGlitchAbility());
        registerGlitch("virus",
                new com.petrol.GlitchSMP.items.VirusGlitchItem(plugin),
                new com.petrol.GlitchSMP.glitches.VirusGlitchAbility());
        // ...add real glitches here...
    }

    // ----------------------------------------------------------------------------------------|
    //
    //
    //
    //
    //
    //
    //
    // DO NOT modify below unless you know what you're doing.                                  |
    // Registry internals handle storage, lookups, and helpers for commands/equip UI.          |
    //
    //
    //
    //
    //
    //
    //
    // ----------------------------------------------------------------------------------------|


    private static Registry instance;
    private final Plugin plugin;
    private final Map<String, ItemAttributes> items = new HashMap<>();
    private final Map<String, AbilityAttributes> abilities = new HashMap<>();

    private Registry(Plugin plugin) {
        this.plugin = plugin;
    }

    public static Registry initialize(Plugin plugin) {
        instance = new Registry(plugin);
        instance.registerGlitches();
        return instance;
    }

    public static Registry get() {
        return instance;
    }

    public Plugin getPlugin() {
        return plugin;
    }
    public void registerGlitch(String id, ItemAttributes itemAttributes, AbilityAttributes abilityAttributes) {
        String key = id.toLowerCase(Locale.ROOT);
        items.put(key, itemAttributes);
        abilities.put(key, abilityAttributes);
    }

    public Optional<ItemAttributes> getItem(String id) {
        return Optional.ofNullable(items.get(id.toLowerCase(Locale.ROOT)));
    }

    public Optional<AbilityAttributes> getAbility(String id) {
        return Optional.ofNullable(abilities.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<ItemAttributes> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public Collection<AbilityAttributes> getAllAbilities() {
        return Collections.unmodifiableCollection(abilities.values());
    }
}
