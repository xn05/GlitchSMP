package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class BedrockGlitchAbility implements AbilityAttributes, Listener {
    private static final long DURATION_ACTIVE_MILLIS = 15_000L;
    private static final long DURATION_UNBREAKABLE_MILLIS = 15_000L;
    private static final long COOLDOWN_SECONDS = 120L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final Map<Location, Long> unbreakableBlocks = new HashMap<>();

    public BedrockGlitchAbility() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getId() {
        return "bedrock";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.WHITE + "Bedrock Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isActive(Player player) {
        Long expiry = activeUntil.get(player.getUniqueId());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        long expiry = System.currentTimeMillis() + DURATION_ACTIVE_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);
        showBossBar(player, expiry);
        player.sendMessage(ChatColor.AQUA + "Bedrock Glitch activated! For 15 seconds, all blocks you place will be unbreakable.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (isActive(player)) {
            long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
            updateBossBar(player, remaining);
        } else {
            clearBossBar(player.getUniqueId());
        }
        // Cleanup expired active players
        activeUntil.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        // Cleanup expired unbreakable blocks
        unbreakableBlocks.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        clearBossBar(player.getUniqueId());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            Location location = event.getBlock().getLocation();
            long expiry = System.currentTimeMillis() + DURATION_UNBREAKABLE_MILLIS;
            unbreakableBlocks.put(location, expiry);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        Long expiry = unbreakableBlocks.get(location);
        if (expiry != null && expiry > System.currentTimeMillis()) {
            event.setCancelled(true);
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
        BossBar bar = Bukkit.createBossBar(ChatColor.WHITE + "Bedrock Glitch", BarColor.WHITE, BarStyle.SEGMENTED_6);
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
        double progress = Math.max(0D, Math.min(1D, remainingMillis / (double) DURATION_ACTIVE_MILLIS));
        bar.setProgress(progress);
        int seconds = (int) Math.ceil(remainingMillis / 1000.0);
        bar.setTitle(ChatColor.WHITE + "Bedrock Glitch (" + seconds + "s)");
    }

    private void clearBossBar(UUID uuid) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }
}
