package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class ActionbarHandler {
    private static final String GLYPH_BLANK = "\uE901"; // glitch_token_g
    private static final String GLYPH_TIMER_S = "\uE001"; // chat/icons/s
    private static final String GLYPH_TIMER_M = "\uE002"; // chat/icons/m

    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final AbilityHandler abilityHandler;
    private final Registry registry;
    private BukkitTask task;

    public ActionbarHandler(com.petrol.GlitchSMP.GlitchSMP plugin, AbilityHandler abilityHandler, Registry registry) {
        this.plugin = plugin;
        this.abilityHandler = abilityHandler;
        this.registry = registry;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(buildBar(player));
            }
        }, 1L, 5L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private String buildBar(Player player) {
        SlotDisplay primary = resolveSlot(player, AbilityHandler.Slot.PRIMARY);
        SlotDisplay secondary = resolveSlot(player, AbilityHandler.Slot.SECONDARY);

        StringBuilder bar = new StringBuilder();
        String timerSegment = formatCooldown(primary.cooldownMillis());
        if (!timerSegment.isEmpty()) {
            bar.append(ChatColor.WHITE).append(timerSegment).append(" ");
        }
        bar.append(ChatColor.WHITE).append(primary.glyph())
                .append(ChatColor.DARK_GRAY).append(" ")
                .append(secondary.glyph());
        return bar.toString();
    }

    private SlotDisplay resolveSlot(Player player, AbilityHandler.Slot slot) {
        return abilityHandler.getEquipped(player, slot)
                .map(ability -> new SlotDisplay(
                        registry.getItem(ability.getId())
                                .map(ItemAttributes::getGlyph)
                                .filter(g -> g != null && !g.isEmpty())
                                .orElse(GLYPH_BLANK),
                        abilityHandler.getCooldownRemainingMillis(player, ability)))
                .orElse(SlotDisplay.EMPTY);
    }

    private String formatCooldown(long millis) {
        if (millis <= 0) {
            return "";
        }
        long totalSeconds = (millis + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        StringBuilder builder = new StringBuilder();
        builder.append(minutes).append(GLYPH_TIMER_M).append(" ");
        if (seconds < 10) {
            builder.append('0');
        }
        builder.append(seconds).append(GLYPH_TIMER_S);
        return builder.toString();
    }

    private record SlotDisplay(String glyph, long cooldownMillis) {
        private static final SlotDisplay EMPTY = new SlotDisplay(GLYPH_BLANK, 0L);
    }
}
