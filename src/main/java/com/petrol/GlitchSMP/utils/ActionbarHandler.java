package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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
        long windowMillis = primary.windowMillis() > 0 ? primary.windowMillis() : secondary.windowMillis();
        long timerMillis = windowMillis > 0 ? windowMillis : (primary.cooldownMillis() > 0 ? primary.cooldownMillis() : secondary.cooldownMillis());
        String timerSegment = windowMillis > 0 ? formatWindow(windowMillis) : formatCooldown(timerMillis);

        StringBuilder bar = new StringBuilder();
        if (!timerSegment.isEmpty()) {
            bar.append(ChatColor.AQUA).append(timerSegment).append(" ");
        }
        bar.append(ChatColor.WHITE).append(primary.glyph())
                .append(ChatColor.DARK_GRAY).append(" ")
                .append(ChatColor.WHITE).append(secondary.glyph());
        return bar.toString().trim();
    }

    private SlotDisplay resolveSlot(Player player, AbilityHandler.Slot slot) {
        return abilityHandler.getEquipped(player, slot)
                .map(ability -> new SlotDisplay(
                        resolveGlyph(ability),
                        abilityHandler.getCooldownRemainingMillis(player, ability),
                        abilityHandler.getActivationWindowMillis(player, ability)))
                .orElse(SlotDisplay.EMPTY);
    }

    private String resolveGlyph(AbilityAttributes ability) {
        return registry.getItem(ability.getId())
                .map(ItemAttributes::getGlyph)
                .filter(g -> g != null && !g.isEmpty())
                .orElseGet(() -> {
                    String fallback = ability.getGlyph();
                    return (fallback == null || fallback.isEmpty()) ? GLYPH_BLANK : fallback;
                });
    }

    private String formatWindow(long millis) {
        if (millis <= 0) {
            return "";
        }
        long seconds = (millis + 999) / 1000;
        return "" + seconds + "\uE001"; // using the seconds glyph for urgency
    }

    private String formatCooldown(long millis) {
        if (millis <= 0) {
            return "";
        }
        long totalSeconds = (millis + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes + GLYPH_TIMER_M + " " + String.format("%02d", seconds) + GLYPH_TIMER_S;
    }

    private record SlotDisplay(String glyph, long cooldownMillis, long windowMillis) {
        private static final SlotDisplay EMPTY = new SlotDisplay(GLYPH_BLANK, 0L, 0L);
    }
}
