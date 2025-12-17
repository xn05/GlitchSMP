package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrashGlitchAbility implements AbilityAttributes {
    private static final long WINDOW_MILLIS = 15_000L;
    private static final long COOLDOWN_SECONDS = 120L;

    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final Map<UUID, Integer> lastSecondsShown = new HashMap<>();
    private static final Map<UUID, Long> lastArmed = new HashMap<>();
    private static final Map<UUID, Long> lastWarning = new HashMap<>();
    private static final Map<UUID, Long> crashTimeouts = new ConcurrentHashMap<>();
    private static boolean listenerRegistered;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeWindows = new HashMap<>();

    public CrashGlitchAbility() {
        if (!listenerRegistered) {
            Bukkit.getPluginManager().registerEvents(new CrashJoinListener(), plugin);
            listenerRegistered = true;
        }
    }

    @Override
    public String getId() {
        return "crash";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Crash Glitch";
    }

    @Override
    public String getGlyph() {
        return "\u221C";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isWindowActive(Player player) {
        Long expires = activeWindows.get(player.getUniqueId());
        return expires != null && expires > System.currentTimeMillis();
    }

    @Override
    public boolean allowWhileCooling(Player player) {
        return isWindowActive(player);
    }

    @Override
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot,
                                             ControlHandler.ActivationAction action) {
        if (isWindowActive(player)) {
            long armedAt = lastArmed.getOrDefault(player.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - armedAt > 500L) {
                long warnedAt = lastWarning.getOrDefault(player.getUniqueId(), 0L);
                if (now - warnedAt > 500L) {
                    player.sendMessage(ChatColor.RED + "Crash Glitch is already armed!");
                    lastWarning.put(player.getUniqueId(), now);
                }
            }
            return TriggerResult.none();
        }
        long expiry = System.currentTimeMillis() + WINDOW_MILLIS;
        activeWindows.put(player.getUniqueId(), expiry);
        lastArmed.put(player.getUniqueId(), System.currentTimeMillis());
        showBossBar(player, expiry);
        player.sendMessage(ChatColor.AQUA + "Crash Glitch armed. Hit a player within 15s!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @Override
    public TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return TriggerResult.none();
        }
        if (!(event.getEntity() instanceof Player target)) {
            return TriggerResult.none();
        }
        if (!isWindowActive(player)) {
            return TriggerResult.none();
        }
        clearWindow(player.getUniqueId());
        applyCrash(target, player);
        player.sendMessage(ChatColor.RED + "You crashed " + target.getName() + "!");
        return TriggerResult.none();
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isWindowActive(player)) {
            clearWindow(player.getUniqueId());
            return TriggerResult.none();
        }
        long remaining = activeWindows.get(player.getUniqueId()) - System.currentTimeMillis();
        updateBossBar(player, remaining);
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        clearWindow(player.getUniqueId());
    }

    private void applyCrash(Player target, Player source) {
        long expiry = System.currentTimeMillis() + WINDOW_MILLIS;
        crashTimeouts.put(target.getUniqueId(), expiry);
        Bukkit.getScheduler().runTask(plugin, () -> {
            String message = ChatColor.RED + "CONNECTION THROTTLED";
            target.kickPlayer(message);
        });
    }

    private void showBossBar(Player player, long expiry) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar created = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Crash Glitch (" + (WINDOW_MILLIS / 1000) + "s)", BarColor.WHITE, BarStyle.SEGMENTED_6);
            created.addPlayer(player);
            return created;
        });
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
        updateBossBar(player, expiry - System.currentTimeMillis());
    }

    private void updateBossBar(Player player, long remainingMillis) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar == null) {
            return;
        }
        if (remainingMillis <= 0) {
            clearWindow(player.getUniqueId());
            return;
        }
        double progress = Math.max(0D, Math.min(1D, remainingMillis / (double) WINDOW_MILLIS));
        bar.setProgress(progress);
        int seconds = (int) Math.ceil(remainingMillis / 1000.0);
        Integer previous = lastSecondsShown.get(player.getUniqueId());
        if (previous == null || previous != seconds) {
            lastSecondsShown.put(player.getUniqueId(), seconds);
            bar.setTitle(ChatColor.LIGHT_PURPLE + "Crash Glitch (" + seconds + "s)");
        }
    }

    private void clearWindow(UUID uuid) {
        activeWindows.remove(uuid);
        BossBar bar = bossBars.remove(uuid);
        lastSecondsShown.remove(uuid);
        lastArmed.remove(uuid);
        lastWarning.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    private static class CrashJoinListener implements Listener {
        @EventHandler
        public void onLogin(PlayerLoginEvent event) {
            Long expires = crashTimeouts.get(event.getPlayer().getUniqueId());
            if (expires == null) {
                return;
            }
            if (System.currentTimeMillis() > expires) {
                crashTimeouts.remove(event.getPlayer().getUniqueId());
                return;
            }
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "CONNECTION THROTTLED");
        }
    }
}
