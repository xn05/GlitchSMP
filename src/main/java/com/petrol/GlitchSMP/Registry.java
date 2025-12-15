package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.AbilityAttributes;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry for glitch definitions, items, and abilities.
 */
public class Registry {
    private final Plugin plugin;
    private final Map<String, GlitchItem<? extends AbilityAttributes>> items = new HashMap<>();
    private final Map<String, AbilityAttributes> abilities = new HashMap<>();

    public Registry(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void bootstrap() {
        // TODO hook into automatic registration or config-driven loading.
    }

    public void registerItem(GlitchItem<? extends AbilityAttributes> item) {
        items.put(item.getId(), item);
        abilities.put(item.getId(), item.getAbility());
    }

    public Optional<GlitchItem<? extends AbilityAttributes>> getItem(String id) {
        return Optional.ofNullable(items.get(id.toLowerCase()));
    }

    public Optional<AbilityAttributes> getAbility(String id) {
        return Optional.ofNullable(abilities.get(id.toLowerCase()));
    }

    public Collection<GlitchItem<? extends AbilityAttributes>> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public Collection<AbilityAttributes> getAllAbilities() {
        return Collections.unmodifiableCollection(abilities.values());
    }
}
