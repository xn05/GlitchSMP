package com.petrol.GlitchSMP.glitches;

import com.petrol.GlitchSMP.Registry;
import com.petrol.GlitchSMP.utils.AbilityAttributes;
import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchanterGlitchAbility implements AbilityAttributes {
    private static final long DURATION_MILLIS = 30_000L; // 30 seconds
    private static final long COOLDOWN_SECONDS = 180L; // 3 minutes

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, Map<Enchantment, Integer>> originalEnchants = new HashMap<>();
    private final Map<UUID, Long> activeUntil = new HashMap<>();

    @Override
    public String getId() {
        return "enchanter";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Enchanter Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
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
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding a tool!");
            return TriggerResult.none();
        }
        if (!isTool(item.getType())) {
            player.sendMessage(ChatColor.RED + "You must be holding a tool!");
            return TriggerResult.none();
        }

        // Store original enchants
        Map<Enchantment, Integer> originals = new HashMap<>(item.getEnchantments());
        originalEnchants.put(player.getUniqueId(), originals);

        // Boost enchants by 1
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Map.Entry<Enchantment, Integer> entry : originals.entrySet()) {
                Enchantment ench = entry.getKey();
                int level = entry.getValue();
                meta.addEnchant(ench, level + 1, true);
            }
            item.setItemMeta(meta);
        }

        long expiry = System.currentTimeMillis() + DURATION_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);

        // Show bossbar
        showBossBar(player, DURATION_MILLIS);

        // Schedule restore
        new BukkitRunnable() {
            @Override
            public void run() {
                restoreEnchants(player);
            }
        }.runTaskLater(plugin, DURATION_MILLIS / 50); // 30 seconds

        player.sendMessage(ChatColor.GREEN + "Enchanter Glitch activated! Tool enchantments boosted for 30 seconds.");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onOffhandSwap(Player player, AbilityHandler.Slot slot) {
        return onControlActivation(player, slot, ControlHandler.ActivationAction.OFFHAND);
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isActive(player)) {
            clearBossBar(player.getUniqueId());
            return TriggerResult.none();
        }
        long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
        updateBossBar(player, remaining);
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        restoreEnchants(player);
        clearBossBar(player.getUniqueId());
    }

    private boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_AXE") || name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
               name.endsWith("_HOE") || name.endsWith("_SWORD") || name.equals("BOW") ||
               name.equals("CROSSBOW") || name.equals("FISHING_ROD") || name.equals("SHEARS") ||
               name.equals("FLINT_AND_STEEL");
    }

    private void showBossBar(Player player, long duration) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar created = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Enchanter Glitch", BarColor.PURPLE, BarStyle.SEGMENTED_6);
            created.addPlayer(player);
            return created;
        });
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        bar.setVisible(true);
        updateBossBar(player, duration);
    }

    private void updateBossBar(Player player, long remainingMillis) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar != null) {
            double progress = Math.max(0D, Math.min(1D, remainingMillis / (double) DURATION_MILLIS));
            bar.setProgress(progress);
            int seconds = (int) Math.ceil(remainingMillis / 1000.0);
            bar.setTitle(ChatColor.LIGHT_PURPLE + "Enchanter Glitch (" + seconds + "s)");
        }
    }

    private void clearBossBar(UUID uuid) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }


    private void restoreEnchants(Player player) {
        Map<Enchantment, Integer> originals = originalEnchants.remove(player.getUniqueId());
        if (originals != null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Remove all enchants and re-add originals
                    for (Enchantment ench : meta.getEnchants().keySet()) {
                        meta.removeEnchant(ench);
                    }
                    for (Map.Entry<Enchantment, Integer> entry : originals.entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                    item.setItemMeta(meta);
                }
            }
        }
        activeUntil.remove(player.getUniqueId());
        clearBossBar(player.getUniqueId());
    }
}
