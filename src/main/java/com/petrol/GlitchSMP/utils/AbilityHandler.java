package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class AbilityHandler implements Listener {
    public enum Slot {
        PRIMARY,
        SECONDARY
    }

    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final Registry registry;
    private final Map<UUID, EnumMap<Slot, String>> equipped = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private BukkitTask tickTask;

    public AbilityHandler(com.petrol.GlitchSMP.GlitchSMP plugin, Registry registry) {
        this.plugin = plugin;
        this.registry = registry;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long tick = plugin.getServer().getCurrentTick();
            for (UUID uuid : equipped.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                for (AbilityAttributes ability : getEquippedAbilities(player)) {
                    AbilityAttributes.TriggerResult result = ability.onTick(player, tick);
                    handleTriggerResult(player, ability, result);
                }
            }
        }, 1L, 1L);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
        }
    }

    public void equip(Player player, Slot slot, String glitchId) {
        EnumMap<Slot, String> slots = equipped.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(Slot.class));
        Slot targetSlot = slot == null ? Slot.PRIMARY : slot;
        String previous = slots.put(targetSlot, glitchId.toLowerCase());
        registry.getAbility(glitchId).ifPresent(attr -> {
            AbilityAttributes.TriggerResult result = attr.onEquip(player);
            handleTriggerResult(player, attr, result);
        });
        if (previous != null && !previous.equalsIgnoreCase(glitchId)) {
            registry.getAbility(previous).ifPresent(attr -> attr.onUnequip(player));
        }
    }

    public void unequip(Player player, Slot slot) {
        EnumMap<Slot, String> slots = equipped.get(player.getUniqueId());
        if (slots == null) {
            return;
        }
        if (slot == null) {
            for (String id : slots.values()) {
                registry.getAbility(id).ifPresent(attr -> attr.onUnequip(player));
            }
            slots.clear();
            return;
        }
        String removed = slots.remove(slot);
        if (removed != null) {
            registry.getAbility(removed).ifPresent(attr -> attr.onUnequip(player));
        }
    }

    public Optional<AbilityAttributes> getEquipped(Player player, Slot slot) {
        EnumMap<Slot, String> slots = equipped.get(player.getUniqueId());
        if (slots == null) {
            return Optional.empty();
        }
        String id = slots.get(slot);
        return id == null ? Optional.empty() : registry.getAbility(id);
    }

    public AbilityAttributes[] getEquippedAbilities(Player player) {
        EnumMap<Slot, String> slots = equipped.get(player.getUniqueId());
        if (slots == null) {
            return new AbilityAttributes[0];
        }
        return slots.values().stream()
                .map(registry::getAbility)
                .flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
                .toArray(AbilityAttributes[]::new);
    }

    public AbilityAttributes[] getEquippedAbilities(Player player, Slot slot) {
        EnumMap<Slot, String> slots = equipped.get(player.getUniqueId());
        if (slots == null) {
            return new AbilityAttributes[0];
        }
        String id = slots.get(slot);
        if (id == null) {
            return new AbilityAttributes[0];
        }
        return registry.getAbility(id).map(a -> new AbilityAttributes[]{a}).orElseGet(() -> new AbilityAttributes[0]);
    }

    private void handleTriggerResult(Player player, AbilityAttributes ability, AbilityAttributes.TriggerResult result) {
        if (result == null || !result.startCooldown()) {
            return;
        }
        long cooldown = result.cooldownMillis() > 0 ? result.cooldownMillis() : ability.getBaseCooldownMillis();
        cooldowns.put(key(player, ability), System.currentTimeMillis() + cooldown);
    }

    public long getCooldownRemainingMillis(Player player, AbilityAttributes ability) {
        Long expiresAt = cooldowns.get(key(player, ability));
        long remaining = expiresAt == null ? 0L : expiresAt - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    private boolean isOnCooldown(Player player, AbilityAttributes ability) {
        return getCooldownRemainingMillis(player, ability) > 0L;
    }

    private String key(Player player, AbilityAttributes ability) {
        return player.getUniqueId() + ":" + ability.getId();
    }

    private void fire(Player player, AbilityAttributes ability, AbilityAttributes.TriggerResult result) {
        handleTriggerResult(player, ability, result);
    }

    private Optional<AbilityAttributes> getAbilityFromEvent(Player player) {
        return getEquipped(player, Slot.PRIMARY).or(() -> getEquipped(player, Slot.SECONDARY));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (isOnCooldown(player, ability)) {
                continue;
            }
            AbilityAttributes.TriggerResult result;
            if (player.isSneaking()) {
                switch (event.getAction()) {
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        result = ability.onShiftRightClick(event);
                        break;
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                        result = ability.onShiftLeftClick(event);
                        break;
                    default:
                        result = AbilityAttributes.TriggerResult.none();
                        break;
                }
            } else {
                switch (event.getAction()) {
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        result = ability.onRightClick(event);
                        break;
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                        result = ability.onLeftClick(event);
                        break;
                    default:
                        result = AbilityAttributes.TriggerResult.none();
                        break;
                }
            }
            fire(player, ability, result);
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            for (AbilityAttributes ability : getEquippedAbilities(damager)) {
                if (!isOnCooldown(damager, ability)) {
                    fire(damager, ability, ability.onEntityHit(event));
                }
            }
        }
        if (event.getEntity() instanceof Player target) {
            for (AbilityAttributes ability : getEquippedAbilities(target)) {
                if (!isOnCooldown(target, ability)) {
                    fire(target, ability, ability.onEntityDamaged(event));
                }
            }
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(event.getPlayer())) {
            if (!isOnCooldown(event.getPlayer(), ability)) {
                fire(event.getPlayer(), ability, ability.onOffhandSwap(event));
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onProjectileLaunch(event));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onProjectileHit(event));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onPlayerMove(event));
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onSneakToggle(event));
            }
        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onInventoryClick(event));
            }
        }
    }

    public void fireCustomEvent(String eventId, Player player, Object payload) {
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!isOnCooldown(player, ability)) {
                fire(player, ability, ability.onCustomEvent(eventId, player, payload));
            }
        }
    }
}
