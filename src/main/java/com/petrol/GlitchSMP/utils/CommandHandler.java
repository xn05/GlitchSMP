package com.petrol.GlitchSMP.utils;

import com.petrol.GlitchSMP.GlitchSMP;
import com.petrol.GlitchSMP.Registry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private static final int GUI_ROWS = 5;
    private static final int GUI_SIZE = GUI_ROWS * 9;
    private static final List<String> SETTINGS_ACTIONS = List.of(
            "RIGHT", "LEFT", "SHIFT_RIGHT", "SHIFT_LEFT", "OFFHAND", "MOVE", "SNEAK"
    );
    private static final Set<String> SETTINGS_LOOKUP = new HashSet<>(SETTINGS_ACTIONS);

    private final GlitchSMP plugin;
    private final Registry registry;
    private final AbilityHandler abilityHandler;

    public CommandHandler(GlitchSMP plugin, Registry registry, AbilityHandler abilityHandler) {
        this.plugin = plugin;
        this.registry = registry;
        this.abilityHandler = abilityHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return sendHelp(sender);
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> handleGive(sender, args);
            case "help" -> sendHelp(sender);
            case "reload" -> handleReload(sender);
            case "glitches" -> handleGlitches(sender);
            case "equip" -> handleEquip(sender, args);
            case "unequip" -> handleUnequip(sender, args);
            case "settings" -> handleSettings(sender, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /glitch help");
                yield true;
            }
        };
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("glitchsmp.give")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch give <player> <id>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        Optional<ItemAttributes> itemAttr = registry.getItem(args[2]);
        if (itemAttr.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown glitch id.");
            return true;
        }
        target.getInventory().addItem(itemAttr.get().createItemStack());
        sender.sendMessage(ChatColor.GREEN + "Gave " + args[2] + " to " + target.getName());
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("glitchsmp.reload")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        plugin.reloadGlitchPlugin();
        sender.sendMessage(ChatColor.GREEN + "GlitchSMP reloaded.");
        return true;
    }

    private boolean handleGlitches(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("glitchsmp.gui")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        Inventory gui = Bukkit.createInventory(player, GUI_SIZE, ChatColor.DARK_GRAY + "Glitches");
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int slot = 0; slot < GUI_SIZE; slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == GUI_ROWS - 1 || col == 0 || col == 8) {
                gui.setItem(slot, filler);
            }
        }
        int index = 0;
        for (ItemAttributes attributes : registry.getAllItems()) {
            ItemStack entry = attributes.createItemStack();
            int slot = slotFromIndex(index++);
            if (slot >= GUI_SIZE) {
                break;
            }
            gui.setItem(slot, entry);
        }
        player.openInventory(gui);
        return true;
    }

    private int slotFromIndex(int index) {
        int row = 1 + index / 7;
        int col = 1 + index % 7;
        return row * 9 + col;
    }

    private boolean handleEquip(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("glitchsmp.equip")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch equip <slot1|slot2> <id>");
            return true;
        }
        AbilityHandler.Slot slot = parseSlot(args[1]);
        if (slot == null) {
            sender.sendMessage(ChatColor.RED + "Invalid slot. Use slot1 or slot2.");
            return true;
        }
        Optional<ItemAttributes> itemAttr = registry.getItem(args[2]);
        Optional<AbilityAttributes> abilityAttr = registry.getAbility(args[2]);
        if (itemAttr.isEmpty() || abilityAttr.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown glitch id.");
            return true;
        }
        ItemStack itemStack = itemAttr.get().createItemStack();
        if (!player.getInventory().containsAtLeast(itemStack, 1)) {
            sender.sendMessage(ChatColor.RED + "You must hold the glitch item to equip it.");
            return true;
        }
        player.getInventory().removeItem(itemStack);
        abilityHandler.equip(player, slot, abilityAttr.get().getId());
        player.sendMessage(ChatColor.GREEN + "Equipped " + abilityAttr.get().getDisplayName() + " in " + args[1]);
        return true;
    }

    private boolean handleUnequip(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("glitchsmp.unequip")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch unequip <slot1|slot2>");
            return true;
        }
        AbilityHandler.Slot slot = parseSlot(args[1]);
        if (slot == null) {
            sender.sendMessage(ChatColor.RED + "Invalid slot. Use slot1 or slot2.");
            return true;
        }
        Optional<AbilityAttributes> equipped = abilityHandler.getEquipped(player, slot);
        if (equipped.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Nothing equipped in that slot.");
            return true;
        }
        if (player.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "Inventory full.");
            return true;
        }
        registry.getItem(equipped.get().getId())
                .ifPresent(item -> player.getInventory().addItem(item.createItemStack()));
        abilityHandler.unequip(player, slot);
        player.sendMessage(ChatColor.YELLOW + "Unequipped " + equipped.get().getDisplayName());
        return true;
    }

    private boolean handleSettings(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("glitchsmp.settings")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /glitch settings <slot1|slot2> <action>");
            return true;
        }
        AbilityHandler.Slot slot = parseSlot(args[1]);
        if (slot == null) {
            sender.sendMessage(ChatColor.RED + "Invalid slot. Use slot1 or slot2.");
            return true;
        }
        String action = args[2].toUpperCase(Locale.ROOT);
        if (!SETTINGS_LOOKUP.contains(action)) {
            sender.sendMessage(ChatColor.RED + "Unknown activation. Options: " + SETTINGS_ACTIONS);
            return true;
        }
        player.sendMessage(ChatColor.GRAY + "Set activation for " + args[1] + " to " + action);
        return true;
    }

    private boolean sendHelp(CommandSender sender) {
        boolean admin = sender.hasPermission("glitchsmp.give") || sender.hasPermission("glitchsmp.reload");
        sender.sendMessage(ChatColor.AQUA + "Glitch Commands:");
        sender.sendMessage(ChatColor.GRAY + "/glitch glitches - view all glitches");
        sender.sendMessage(ChatColor.GRAY + "/glitch equip <slot> <id> - equip a glitch");
        sender.sendMessage(ChatColor.GRAY + "/glitch unequip <slot> - remove a glitch");
        sender.sendMessage(ChatColor.GRAY + "/glitch settings <slot> <action> - configure activation");
        if (admin) {
            sender.sendMessage(ChatColor.GOLD + "Admin Commands:");
            sender.sendMessage(ChatColor.GOLD + "/glitch give <player> <id>");
            sender.sendMessage(ChatColor.GOLD + "/glitch reload");
        }
        return true;
    }

    private AbilityHandler.Slot parseSlot(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "slot1", "1", "primary" -> AbilityHandler.Slot.PRIMARY;
            case "slot2", "2", "secondary" -> AbilityHandler.Slot.SECONDARY;
            default -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("give", "help", "reload", "glitches", "equip", "unequip", "settings"), args[0]);
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> {
                if (args.length == 2) {
                    return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
                }
                if (args.length == 3) {
                    return filter(registry.getAllItems().stream().map(ItemAttributes::getId).toList(), args[2]);
                }
            }
            case "equip", "unequip", "settings" -> {
                if (args.length == 2) {
                    return filter(Arrays.asList("slot1", "slot2"), args[1]);
                }
                if (args[0].equalsIgnoreCase("equip") && args.length == 3) {
                    return filter(registry.getAllItems().stream().map(ItemAttributes::getId).toList(), args[2]);
                }
                if (args[0].equalsIgnoreCase("settings") && args.length == 3) {
                    return filter(SETTINGS_ACTIONS, args[2]);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String token) {
        if (token == null || token.isEmpty()) {
            return options;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
