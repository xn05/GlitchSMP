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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvisGlitchAbility implements AbilityAttributes {
    private static final long DURATION_MILLIS = 30_000L; // 30 seconds
    private static final long COOLDOWN_SECONDS = 240L; // 4 minutes

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    @Override
    public String getId() {
        return "invis";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GRAY + "Invis Glitch";
    }

    @Override
    public String getGlyph() {
        return "Ⅷ";
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
    public TriggerResult onControlActivation(Player player, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        if (isActive(player)) {
            player.sendMessage(ChatColor.RED + "Invis Glitch is already active!");
            return TriggerResult.none();
        }
        long expiry = System.currentTimeMillis() + DURATION_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);
        player.setInvisible(true);
        // Create bossbar
        BossBar bossBar = Bukkit.createBossBar(ChatColor.GRAY + "Invisible - 30s left", BarColor.WHITE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        activeBossBars.put(player.getUniqueId(), bossBar);
        player.sendMessage(ChatColor.AQUA + "You are now invisible for 30 seconds!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isActive(player)) {
            activeUntil.remove(player.getUniqueId());
            player.setInvisible(false);
            BossBar bar = activeBossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
            return TriggerResult.none();
        }
        // Update bossbar
        long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
        double progress = Math.max(0, remaining / (double) DURATION_MILLIS);
        int seconds = (int) Math.ceil(remaining / 1000.0);
        BossBar bar = activeBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setProgress(progress);
            bar.setTitle(ChatColor.GRAY + "Invisible - " + seconds + "s left");
        }
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        activeUntil.remove(player.getUniqueId());
        player.setInvisible(false);
        BossBar bar = activeBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }
}
