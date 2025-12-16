package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ActionbarHandler {
    private static final String GLYPH_BLANK = "\uE901"; // blank slot texture
    private static final String GLYPH_TIMER_S = "\uE001"; // seconds glyph
    private static final String GLYPH_TIMER_M = "\uE002"; // minutes glyph
    private static final char[] SUPER_DIGITS = {'\u2070', '\u00B9', '\u00B2', '\u00B3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'};

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
        String primarySegment = formatSlot(resolveSlot(player, AbilityHandler.Slot.PRIMARY));
        String secondarySegment = formatSlot(resolveSlot(player, AbilityHandler.Slot.SECONDARY));
        return (primarySegment + ChatColor.DARK_GRAY + " " + secondarySegment).trim();
    }

    private SlotDisplay resolveSlot(Player player, AbilityHandler.Slot slot) {
        return abilityHandler.getEquipped(player, slot)
                .map(ability -> new SlotDisplay(
                        resolveGlyph(ability),
                        abilityHandler.getCooldownRemainingMillis(player, ability)))
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

    private String formatSlot(SlotDisplay slot) {
        StringBuilder sb = new StringBuilder();
        if (slot.cooldownMillis() > 0) {
            sb.append(ChatColor.AQUA).append(formatCooldown(slot.cooldownMillis())).append(" ");
        }
        sb.append(ChatColor.WHITE).append(slot.glyph());
        return sb.toString();
    }

    private String formatCooldown(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        if (seconds >= 60L) {
            long minutes = seconds / 60L;
            long rem = seconds % 60L;
            if (rem == 0L) {
                return toSuperscript(minutes) + GLYPH_TIMER_M;
            }
            return toSuperscript(minutes) + GLYPH_TIMER_M + ChatColor.GRAY + " " + ChatColor.AQUA + toSuperscript(rem) + GLYPH_TIMER_S;
        }
        return toSuperscript(seconds) + GLYPH_TIMER_S;
    }

    private String toSuperscript(long value) {
        if (value == 0) {
            return String.valueOf(SUPER_DIGITS[0]);
        }
        StringBuilder sb = new StringBuilder();
        long remaining = value;
        while (remaining > 0) {
            int digit = (int) (remaining % 10);
            sb.insert(0, SUPER_DIGITS[digit]);
            remaining /= 10;
        }
        return sb.toString();
    }

    private record SlotDisplay(String glyph, long cooldownMillis) {
        private static final SlotDisplay EMPTY = new SlotDisplay(GLYPH_BLANK, 0L);
    }
}
