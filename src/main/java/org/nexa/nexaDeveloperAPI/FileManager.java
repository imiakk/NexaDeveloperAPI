package org.nexa.nexaDeveloperAPI;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration cfg;

    public FileManager(JavaPlugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.file = new File(plugin.getDataFolder(), "data.yaml");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public String getToken() {
        return cfg.getString("gameToken", "");
    }

}