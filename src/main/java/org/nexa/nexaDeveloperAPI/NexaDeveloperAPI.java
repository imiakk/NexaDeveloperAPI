package org.nexa.nexaDeveloperAPI;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexaDeveloperAPI extends JavaPlugin implements Listener {

    private NexaAPI nexaAPI;
    private FileManager manager;

    @Override
    public void onEnable() {
        this.manager = new FileManager(this);
        this.nexaAPI = new NexaAPI(this, manager);

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Nexa Developer API Starting...");
    }

    public NexaAPI getNexaAPI() {
        return nexaAPI;
    }

    @Override
    public void onDisable() {
        getLogger().info("Nexa Developer API Shutting down...");
    }
}