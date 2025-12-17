package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirusGlitchAbility implements AbilityAttributes {
    private static final long WINDOW_MILLIS = 15_000L; // 15 seconds
    private static final long COOLDOWN_SECONDS = 120L; // 2 minutes

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Map<UUID, Boolean> hasHit = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    @Override
    public String getId() {
        return "virus";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Virus Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isActive(Player player) {
        Long until = activeUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            player.sendMessage(ChatColor.RED + "Virus Glitch is already active!");
            return TriggerResult.none();
        }
        long expiry = System.currentTimeMillis() + WINDOW_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);
        hasHit.put(player.getUniqueId(), false);
        // Create bossbar
        BossBar bossBar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Virus Glitch Active - Hit to Infect!", BarColor.PURPLE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        activeBossBars.put(player.getUniqueId(), bossBar);
        player.sendMessage(ChatColor.AQUA + "Virus Glitch activated for 15 seconds!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return TriggerResult.none();
        }
        if (!(event.getDamager() instanceof Player attacker)) {
            return TriggerResult.none();
        }
        if (!isActive(attacker) || hasHit.getOrDefault(attacker.getUniqueId(), false)) {
            return TriggerResult.none();
        }
        // Apply the virus effect
        hasHit.put(attacker.getUniqueId(), true);
        activeUntil.remove(attacker.getUniqueId());
        // Remove bossbar
        BossBar bar = activeBossBars.remove(attacker.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
        flashTitle(target);
        attacker.sendMessage(ChatColor.GREEN + "Virus applied to " + target.getName() + "!");
        target.sendMessage(ChatColor.RED + "You have been infected with the Virus Glitch!"); // Confirm to victim
        return TriggerResult.none();
    }

    private void flashTitle(Player player) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int totalTicks = 80; // 4 seconds

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    this.cancel();
                    return;
                }
                if (Math.random() < 0.1) { // Randomly flash (10% chance per tick)
                    player.sendTitle("", "", 0, 20, 10); // Longer flash: stay 20 ticks (1 second), fade out 10 ticks
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isActive(player)) {
            activeUntil.remove(player.getUniqueId());
            hasHit.remove(player.getUniqueId());
            BossBar bar = activeBossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
            return TriggerResult.none();
        }
        // Update bossbar
        long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
        double progress = Math.max(0, remaining / (double) WINDOW_MILLIS);
        BossBar bar = activeBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setProgress(progress);
        }
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        activeUntil.remove(player.getUniqueId());
        hasHit.remove(player.getUniqueId());
        BossBar bar = activeBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    @Override
    public boolean allowWhileCooling(Player player) {
        return isActive(player);
    }
}
