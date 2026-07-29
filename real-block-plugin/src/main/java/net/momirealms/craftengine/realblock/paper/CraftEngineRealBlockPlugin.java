package net.momirealms.craftengine.realblock.paper;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.realblock.api.CraftEngineRealBlockApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftEngineRealBlockPlugin extends JavaPlugin {
    private RealBlockManager realBlockManager;

    @Override
    public void onEnable() {
        this.realBlockManager = new RealBlockManager(this, BukkitCraftEngine.instance());
        this.realBlockManager.enable();
        Bukkit.getServicesManager().register(
                CraftEngineRealBlockApi.class,
                this.realBlockManager,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        if (this.realBlockManager == null) {
            return;
        }
        Bukkit.getServicesManager().unregister(
                CraftEngineRealBlockApi.class,
                this.realBlockManager
        );
        this.realBlockManager.disable();
        this.realBlockManager = null;
    }
}
