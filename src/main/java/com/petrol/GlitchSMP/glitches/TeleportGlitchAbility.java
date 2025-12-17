package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TeleportGlitchAbility implements AbilityAttributes {
    private static final long COOLDOWN_SECONDS = 120L; // 2 minutes

    private final Plugin plugin = Registry.get().getPlugin();

    @Override
    public String getId() {
        return "teleport";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Teleport Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot,
                                             ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        Block target = player.getTargetBlockExact(50);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No block in range!");
            return TriggerResult.none();
        }
        Location from = player.getLocation();
        Location to = target.getLocation().add(0.5, 0, 0.5); // center on block
        player.teleport(to);

        // Spawn ender particles at from and to
        player.getWorld().spawnParticle(Particle.PORTAL, from, 50, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.PORTAL, to, 50, 0.5, 0.5, 0.5, 0);

        // Animate the particle line
        animateParticleLine(from, to);

        return TriggerResult.consume(getCooldownMillis());
    }

    private void animateParticleLine(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        if (distance == 0) return;
        direction.normalize();

        new BukkitRunnable() {
            private int tick = 0;
            private final int totalTicks = 40; // 2 seconds

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    this.cancel();
                    return;
                }
                double progress = (double) tick / totalTicks;
                Location currentStart = from.clone().add(direction.clone().multiply(distance * progress));
                spawnParticleLine(currentStart, to);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnParticleLine(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        double dist = dir.length();
        if (dist == 0) return;
        dir.normalize();
        int steps = (int) Math.ceil(dist);
        for (int i = 0; i <= steps; i++) {
            Location loc = start.clone().add(dir.clone().multiply(i));
            start.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
        }
    }
}
