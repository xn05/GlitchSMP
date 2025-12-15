package com.petrol.GlitchSMP.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ActionbarHandler {
    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final AbilityHandler abilityHandler;
    private BukkitTask task;

    public ActionbarHandler(com.petrol.GlitchSMP.GlitchSMP plugin, AbilityHandler abilityHandler) {
        this.plugin = plugin;
        this.abilityHandler = abilityHandler;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                abilityHandler.getEquipped(player).ifPresentOrElse(ability -> {
                    player.sendActionBar(ChatColor.AQUA + "Equipped Glitch: " + ChatColor.WHITE + ability.getDisplayName());
                }, () -> player.sendActionBar(""));
            }
        }, 1L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
