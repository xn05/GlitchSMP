package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class AbilityHandler implements Listener {
    public enum Slot {
        PRIMARY,
        SECONDARY
    }

    private final com.petrol.GlitchSMP.GlitchSMP plugin;
    private final Registry registry;
    private final DataHandler dataHandler;
    private final ControlHandler controlHandler;
    private final Map<UUID, EnumMap<Slot, String>> equipped = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private BukkitTask tickTask;

    public AbilityHandler(com.petrol.GlitchSMP.GlitchSMP plugin, Registry registry, DataHandler dataHandler, ControlHandler controlHandler) {
        this.plugin = plugin;
        this.registry = registry;
        this.dataHandler = dataHandler;
        this.controlHandler = controlHandler;
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
        dataHandler.setEquipped(player.getUniqueId(), targetSlot, glitchId);
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
            for (Slot each : Slot.values()) {
                String id = slots.remove(each);
                if (id != null) {
                    registry.getAbility(id).ifPresent(attr -> attr.onUnequip(player));
                    dataHandler.setEquipped(player.getUniqueId(), each, null);
                }
            }
            return;
        }
        String removed = slots.remove(slot);
        if (removed != null) {
            registry.getAbility(removed).ifPresent(attr -> attr.onUnequip(player));
            dataHandler.setEquipped(player.getUniqueId(), slot, null);
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
        if (result == null) {
            return;
        }
        if (result.startCooldown()) {
            long cooldown = result.cooldownMillis() > 0 ? result.cooldownMillis() : ability.getCooldownMillis();
            if (cooldown <= 0) {
                cooldown = ability.getCooldownMillis();
            }
            long expiresAt = System.currentTimeMillis() + Math.max(0L, cooldown);
            cooldowns.put(key(player, ability), expiresAt);
        }
    }

    public long getCooldownRemainingMillis(Player player, AbilityAttributes ability) {
        Long expiresAt = cooldowns.get(key(player, ability));
        if (expiresAt == null) {
            return 0L;
        }
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(key(player, ability));
            return 0L;
        }
        return remaining;
    }

    private boolean isOnCooldown(Player player, AbilityAttributes ability) {
        return getCooldownRemainingMillis(player, ability) > 0L;
    }

    private boolean canTrigger(Player player, AbilityAttributes ability) {
        return player != null && (!isOnCooldown(player, ability) || ability.allowWhileCooling(player));
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
        if (!player.isSneaking()) {
            return;
        }
        ControlHandler.ActivationAction action = switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> ControlHandler.ActivationAction.SHIFT_RIGHT;
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> ControlHandler.ActivationAction.SHIFT_LEFT;
            default -> null;
        };
        if (action != null) {
            resolveAndActivate(player, action, event);
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager) {
            for (AbilityAttributes ability : getEquippedAbilities(damager)) {
                if (canTrigger(damager, ability)) {
                    fire(damager, ability, ability.onEntityHit(event));
                }
            }
        }
        if (event.getEntity() instanceof Player target) {
            for (AbilityAttributes ability : getEquippedAbilities(target)) {
                if (canTrigger(target, ability)) {
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
        handleActivation(event.getPlayer(), ControlHandler.ActivationAction.OFFHAND, ability -> ability.onOffhandSwap(event));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (canTrigger(player, ability)) {
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
            if (canTrigger(player, ability)) {
                fire(player, ability, ability.onProjectileHit(event));
            }
        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (canTrigger(player, ability)) {
                fire(player, ability, ability.onInventoryClick(event));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        hydratePlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        equipped.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EnumMap<Slot, String> slots = equipped.get(player.getUniqueId());
        if (slots != null) {
            for (String id : slots.values()) {
                if (id != null) {
                    registry.getItem(id).ifPresent(item -> {
                        org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(item.getMaterial());
                        org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(item.getDisplayName());
                            meta.setLore(item.getLore());
                            meta.setCustomModelData(item.getCustomModelData());
                            itemStack.setItemMeta(meta);
                        }
                        event.getDrops().add(itemStack);
                    });
                }
            }
        }
    }

    public void fireCustomEvent(String eventId, Player player, Object payload) {
        for (AbilityAttributes ability : getEquippedAbilities(player)) {
            if (!canTrigger(player, ability)) {
                continue;
            }
            fire(player, ability, ability.onCustomEvent(eventId, player, payload));
        }
    }

    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hydratePlayer(player);
        }
    }

    private void hydratePlayer(Player player) {
        controlHandler.hydrate(player);
        EnumMap<Slot, String> slots = equipped.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(Slot.class));
        for (Slot slot : Slot.values()) {
            dataHandler.getEquipped(player.getUniqueId(), slot).ifPresentOrElse(id -> {
                slots.put(slot, id);
                registry.getAbility(id).ifPresent(attr -> attr.onEquip(player));
            }, () -> slots.remove(slot));
        }
    }

    private void handleActivation(Player player, ControlHandler.ActivationAction action,
                                  Function<AbilityAttributes, AbilityAttributes.TriggerResult> executor) {
        for (Slot slot : Slot.values()) {
            if (!controlHandler.matches(player, slot, action)) {
                continue;
            }
            Optional<AbilityAttributes> abilityOpt = getEquipped(player, slot);
            if (abilityOpt.isEmpty()) {
                continue;
            }
            AbilityAttributes ability = abilityOpt.get();
            if (!canTrigger(player, ability)) {
                continue;
            }
            AbilityAttributes.TriggerResult result = executor.apply(ability);
            fire(player, ability, result);
        }
    }

    private void triggerSlot(Player player, Slot slot, ControlHandler.ActivationAction action,
                             java.util.function.BiFunction<AbilityAttributes, Slot, AbilityAttributes.TriggerResult> executor) {
        Optional<AbilityAttributes> abilityOpt = getEquipped(player, slot);
        if (abilityOpt.isEmpty()) {
            return;
        }
        AbilityAttributes ability = abilityOpt.get();
        if (!canTrigger(player, ability)) {
            return;
        }
        AbilityAttributes.TriggerResult result = executor.apply(ability, slot);
        fire(player, ability, result);
    }

    private void resolveAndActivate(Player player, ControlHandler.ActivationAction action, PlayerInteractEvent event) {
        for (Slot slot : Slot.values()) {
            if (!controlHandler.matches(player, slot, action)) {
                continue;
            }
            triggerSlot(player, slot, action, (ability, matchedSlot) ->
                    ability.onControlActivation(event, matchedSlot, action));
        }
    }

    private void resolveAndActivate(Player player, ControlHandler.ActivationAction action,
                                    java.util.function.Function<AbilityAttributes, AbilityAttributes.TriggerResult> executor) {
        for (Slot slot : Slot.values()) {
            if (!controlHandler.matches(player, slot, action)) {
                continue;
            }
            triggerSlot(player, slot, action, (ability, __) -> executor.apply(ability));
        }
    }
}
