package com.petrol.GlitchSMP;

import com.petrol.GlitchSMP.utils.AbilityHandler;
import com.petrol.GlitchSMP.utils.ActionbarHandler;
import com.petrol.GlitchSMP.utils.CommandHandler;
import com.petrol.GlitchSMP.utils.EquipHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlitchSMP extends JavaPlugin {
    private Registry registry;
    private AbilityHandler abilityHandler;
    private ActionbarHandler actionbarHandler;
    private EquipHandler equipHandler;

    @Override
    public void onEnable() {
        registry = new Registry(this);
        abilityHandler = new AbilityHandler(this, registry);
        actionbarHandler = new ActionbarHandler(this, abilityHandler);
        equipHandler = new EquipHandler(registry, abilityHandler);

        registry.bootstrap();
        abilityHandler.start();
        actionbarHandler.start();
        Bukkit.getPluginManager().registerEvents(equipHandler, this);

        CommandHandler commandHandler = new CommandHandler(this, registry, abilityHandler);
        if (getCommand("glitch") != null) {
            getCommand("glitch").setExecutor(commandHandler);
            getCommand("glitch").setTabCompleter(commandHandler);
        }
    }

    @Override
    public void onDisable() {
        if (actionbarHandler != null) {
            actionbarHandler.stop();
        }
        if (abilityHandler != null) {
            abilityHandler.stop();
        }
        registry = null;
    }
}
