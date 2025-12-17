package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.GlitchSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DataHandler {
    private final GlitchSMP plugin;
    private final File dataFile;
    private FileConfiguration config;

    public DataHandler(GlitchSMP plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        try {
            if (!dataFile.getParentFile().exists() && !dataFile.getParentFile().mkdirs()) {
                plugin.getLogger().warning("Unable to create data folder: " + dataFile.getParent());
            }
            if (!dataFile.exists() && !dataFile.createNewFile()) {
                plugin.getLogger().warning("Unable to create data file: " + dataFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to prepare data file", e);
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void reload() {
        load();
    }

    public synchronized void save() {
        if (config == null) {
            return;
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data", e);
        }
    }

    public Optional<String> getEquipped(UUID uuid, AbilityHandler.Slot slot) {
        ensureConfig();
        String value = config.getString(slotPath(uuid, slot));
        return (value == null || value.isEmpty()) ? Optional.empty() : Optional.of(value);
    }

    public void setEquipped(UUID uuid, AbilityHandler.Slot slot, String glitchId) {
        ensureConfig();
        config.set(slotPath(uuid, slot), glitchId == null ? null : glitchId.toLowerCase(Locale.ROOT));
        save();
    }

    public void clearPlayer(UUID uuid) {
        ensureConfig();
        config.set("players." + uuid, null);
        save();
    }

    public Optional<String> getActivation(UUID uuid, AbilityHandler.Slot slot) {
        ensureConfig();
        String value = config.getString(activationPath(uuid, slot));
        return (value == null || value.isEmpty()) ? Optional.empty() : Optional.of(value);
    }

    public void setActivation(UUID uuid, AbilityHandler.Slot slot, String action) {
        ensureConfig();
        // clear the existing activation node before writing to guarantee we overwrite old data
        String root = "players." + uuid + ".activation." + (slot == AbilityHandler.Slot.PRIMARY ? "primary" : "secondary");
        config.set(root, null);
        config.set(root, action == null ? null : action.toUpperCase(Locale.ROOT));
        save();
    }

    public void setData(UUID uuid, String key, String value) {
        config.set("players." + uuid + ".data." + key, value);
        save();
    }

    public String getData(UUID uuid, String key) {
        return config.getString("players." + uuid + ".data." + key);
    }

    private void ensureConfig() {
        if (config == null) {
            load();
        }
    }

    private String slotPath(UUID uuid, AbilityHandler.Slot slot) {
        return "players." + uuid + ".slots." + (slot == AbilityHandler.Slot.PRIMARY ? "primary" : "secondary");
    }

    private String activationPath(UUID uuid, AbilityHandler.Slot slot) {
        return "players." + uuid + ".activation." + (slot == AbilityHandler.Slot.PRIMARY ? "primary" : "secondary");
    }
}
