package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ActionbarHandler;
import com.petrol.GlitchSMP.utils.CommandHandler;
import com.petrol.GlitchSMP.utils.EquipHandler;
import com.petrol.GlitchSMP.utils.DataHandler;
import com.petrol.GlitchSMP.utils.ControlHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class GlitchSMP extends JavaPlugin {
    private Registry registry;
    private AbilityHandler abilityHandler;
    private ActionbarHandler actionbarHandler;
    private EquipHandler equipHandler;
    private DataHandler dataHandler;
    private ControlHandler controlHandler;
    private GuiProtectionListener guiProtectionListener;

    @Override
    public void onEnable() {
        registry = Registry.initialize(this);
        dataHandler = new DataHandler(this);
        dataHandler.load();

        controlHandler = new ControlHandler(dataHandler);
        Bukkit.getPluginManager().registerEvents(controlHandler, this);

        abilityHandler = new AbilityHandler(this, registry, dataHandler, controlHandler);
        actionbarHandler = new ActionbarHandler(this, abilityHandler, registry);
        equipHandler = new EquipHandler(registry, abilityHandler);

        abilityHandler.start();
        abilityHandler.loadOnlinePlayers();
        actionbarHandler.start();
        Bukkit.getPluginManager().registerEvents(equipHandler, this);
        registerDefaultPermissions();

        CommandHandler commandHandler = new CommandHandler(this, registry, abilityHandler, controlHandler);
        registerCommand(new GlitchCommand(commandHandler));
        guiProtectionListener = new GuiProtectionListener(commandHandler);
        Bukkit.getPluginManager().registerEvents(guiProtectionListener, this);
    }

    @Override
    public void onDisable() {
        if (actionbarHandler != null) {
            actionbarHandler.stop();
        }
        if (abilityHandler != null) {
            abilityHandler.stop();
        }
        if (dataHandler != null) {
            dataHandler.save();
        }
        registry = null;
        controlHandler = null;
        guiProtectionListener = null;
    }

    public void reloadGlitchPlugin() {
        if (actionbarHandler != null) {
            actionbarHandler.stop();
        }
        if (abilityHandler != null) {
            abilityHandler.stop();
        }
        if (dataHandler != null) {
            dataHandler.save();
        }
        registry = null;
        actionbarHandler = null;
        abilityHandler = null;
        equipHandler = null;

        if (dataHandler == null) {
            dataHandler = new DataHandler(this);
            dataHandler.load();
        } else {
            dataHandler.reload();
        }

        registry = Registry.initialize(this);
        controlHandler = new ControlHandler(dataHandler);
        Bukkit.getPluginManager().registerEvents(controlHandler, this);

        abilityHandler = new AbilityHandler(this, registry, dataHandler, controlHandler);
        actionbarHandler = new ActionbarHandler(this, abilityHandler, registry);
        equipHandler = new EquipHandler(registry, abilityHandler);
        abilityHandler.start();
        abilityHandler.loadOnlinePlayers();
        actionbarHandler.start();
        Bukkit.getPluginManager().registerEvents(equipHandler, this);

        CommandHandler commandHandler = new CommandHandler(this, registry, abilityHandler, controlHandler);
        registerCommand(new GlitchCommand(commandHandler));
        guiProtectionListener = new GuiProtectionListener(commandHandler);
        Bukkit.getPluginManager().registerEvents(guiProtectionListener, this);
    }

    private void registerCommand(GlitchCommand command) {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            String fallback = getPluginMeta().getDisplayName().toLowerCase();
            commandMap.register(fallback, command);
        } catch (ReflectiveOperationException e) {
            getLogger().severe("Failed to register /glitch command: " + e.getMessage());
        }
    }

    private void registerDefaultPermissions() {
        List<Permission> defaults = Arrays.asList(
                new Permission("glitchsmp.settings", PermissionDefault.TRUE),
                new Permission("glitchsmp.gui", PermissionDefault.TRUE),
                new Permission("glitchsmp.equip", PermissionDefault.TRUE),
                new Permission("glitchsmp.unequip", PermissionDefault.TRUE)
        );
        for (Permission permission : defaults) {
            if (Bukkit.getPluginManager().getPermission(permission.getName()) == null) {
                Bukkit.getPluginManager().addPermission(permission);
            }
        }
    }

    public AbilityHandler getAbilityHandler() {
        return abilityHandler;
    }
}
