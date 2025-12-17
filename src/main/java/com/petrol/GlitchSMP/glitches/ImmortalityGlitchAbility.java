package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImmortalityGlitchAbility implements AbilityAttributes, Listener {
    private static final long DURATION_IMMORTAL_MILLIS = 30_000L;
    private static final long COOLDOWN_SECONDS = 300L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> immortalUntil = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public ImmortalityGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "immortality";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.YELLOW + "Immortality Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isImmortal(Player player) {
        Long expiry = immortalUntil.get(player.getUniqueId());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        long expiry = System.currentTimeMillis() + DURATION_IMMORTAL_MILLIS;
        immortalUntil.put(player.getUniqueId(), expiry);
        showBossBar(player, expiry);
        player.sendMessage(ChatColor.AQUA + "Immortality Glitch activated! You are immortal for 30 seconds.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (isImmortal(player)) {
            long remaining = immortalUntil.get(player.getUniqueId()) - System.currentTimeMillis();
            updateBossBar(player, remaining);
        } else {
            clearBossBar(player.getUniqueId());
        }
        // Cleanup expired
        immortalUntil.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        clearBossBar(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isImmortal(player)) {
                event.setCancelled(true);
            }
        }
    }

    private void showBossBar(Player player, long expiry) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> createBar(player));
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
        updateBossBar(player, expiry - System.currentTimeMillis());
    }

    private BossBar createBar(Player player) {
        BossBar bar = Bukkit.createBossBar(ChatColor.YELLOW + "Immortality Glitch", BarColor.YELLOW, BarStyle.SEGMENTED_6);
        bar.addPlayer(player);
        return bar;
    }

    private void updateBossBar(Player player, long remainingMillis) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar == null) {
            return;
        }
        if (remainingMillis <= 0) {
            clearBossBar(player.getUniqueId());
            return;
        }
        double progress = Math.max(0D, Math.min(1D, remainingMillis / (double) DURATION_IMMORTAL_MILLIS));
        bar.setProgress(progress);
        int seconds = (int) Math.ceil(remainingMillis / 1000.0);
        bar.setTitle(ChatColor.YELLOW + "Immortality Glitch (" + seconds + "s)");
    }

    private void clearBossBar(UUID uuid) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }
}
