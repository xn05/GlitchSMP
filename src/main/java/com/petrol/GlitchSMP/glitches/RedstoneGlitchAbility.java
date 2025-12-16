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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RedstoneGlitchAbility implements AbilityAttributes {
    private static final long DURATION_MILLIS = 30_000L;
    private static final long COOLDOWN_SECONDS = 150L;

    private final Plugin plugin = Registry.get().getPlugin();
    private final Map<UUID, Long> activeUntil = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Override
    public String getId() {
        return "redstone";
    }

    @Override
    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + "Redstone Glitch";
    }

    @Override
    public String getGlyph() {
        return "";
    }

    @Override
    public long getCooldownSeconds() {
        return COOLDOWN_SECONDS;
    }

    private boolean isActive(Player player) {
        Long expires = activeUntil.get(player.getUniqueId());
        return expires != null && expires > System.currentTimeMillis();
    }

    @Override
    public TriggerResult onControlActivation(PlayerInteractEvent event, AbilityHandler.Slot slot, ControlHandler.ActivationAction action) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            player.sendMessage(ChatColor.RED + "Redstone Glitch is already active.");
            return TriggerResult.none();
        }
        long expiry = System.currentTimeMillis() + DURATION_MILLIS;
        activeUntil.put(player.getUniqueId(), expiry);
        showBossBar(player, expiry);
        player.sendMessage(ChatColor.AQUA + "Redstone Glitch activated for 30 seconds!");
        return TriggerResult.consume(getCooldownMillis());
    }

    @Override
    public TriggerResult onEntityHit(EntityDamageByEntityEvent event) {
        Player player = resolvePlayer(event);
        if (player == null || !isActive(player)) {
            return TriggerResult.none();
        }
        double extra = computeExtraDamage(player);
        if (extra <= 0) {
            return TriggerResult.none();
        }
        if (event.getEntity() instanceof LivingEntity target) {
            extra = applyVanillaMitigation(target, event, extra);
        }
        if (extra <= 0) {
            return TriggerResult.none();
        }
        event.setDamage(event.getDamage() + extra);
        return TriggerResult.none();
    }

    @Override
    public TriggerResult onTick(Player player, long tick) {
        if (!isActive(player)) {
            clearBuff(player.getUniqueId());
            return TriggerResult.none();
        }
        long remaining = activeUntil.get(player.getUniqueId()) - System.currentTimeMillis();
        updateBossBar(player, remaining);
        return TriggerResult.none();
    }

    @Override
    public void onUnequip(Player player) {
        clearBuff(player.getUniqueId());
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
        BossBar bar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "Redstone Glitch", BarColor.RED, BarStyle.SEGMENTED_6);
        bar.addPlayer(player);
        return bar;
    }

    private void updateBossBar(Player player, long remainingMillis) {
        BossBar bar = bossBars.get(player.getUniqueId());
        if (bar == null) {
            return;
        }
        if (remainingMillis <= 0) {
            clearBuff(player.getUniqueId());
            return;
        }
        double progress = Math.max(0D, Math.min(1D, remainingMillis / (double) DURATION_MILLIS));
        bar.setProgress(progress);
        int seconds = (int) Math.ceil(remainingMillis / 1000.0);
        bar.setTitle(ChatColor.LIGHT_PURPLE + "Redstone Glitch (" + seconds + "s)");
    }

    private void clearBuff(UUID uuid) {
        activeUntil.remove(uuid);
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    private Player resolvePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }

    private double computeExtraDamage(Player player) {
        int power = countRedstoneBlocks(player);
        return Math.max(0, power) / 100.0;
    }

    private double applyVanillaMitigation(LivingEntity target, EntityDamageByEntityEvent event, double damage) {
        if (damage <= 0) {
            return 0;
        }
        double armor = getAttributeValue(target, Attribute.ARMOR);
        double toughness = getAttributeValue(target, Attribute.ARMOR_TOUGHNESS);
        double armorFactor = Math.min(20.0, Math.max(armor / 5.0, armor - damage / (2.0 + toughness / 4.0))) / 25.0;
        double afterArmor = damage * (1.0 - armorFactor);
        boolean projectile = event.getDamager() instanceof Projectile;
        int protectionPoints = computeProtectionPoints(target, projectile);
        double enchantFactor = Math.min(20, protectionPoints) / 25.0;
        return afterArmor * (1.0 - enchantFactor);
    }

    private double getAttributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }

    private int computeProtectionPoints(LivingEntity target, boolean projectileDamage) {
        EntityEquipment equipment = target.getEquipment();
        if (equipment == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack piece : equipment.getArmorContents()) {
            if (piece == null) {
                continue;
            }
            total += piece.getEnchantmentLevel(Enchantment.PROTECTION);
            if (projectileDamage) {
                total += piece.getEnchantmentLevel(Enchantment.PROJECTILE_PROTECTION) * 2;
            }
        }
        return total;
    }

    private int countRedstoneBlocks(Player player) {
        int total = 0;
        for (org.bukkit.inventory.ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.REDSTONE_BLOCK) {
                total += stack.getAmount();
            }
        }
        org.bukkit.inventory.ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() == Material.REDSTONE_BLOCK) {
            total += offhand.getAmount();
        }
        return total;
    }
}
