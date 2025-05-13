package com.rafaxplugins.announce.misc.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class CustomConfig extends YamlConfiguration {

    private final Plugin plugin;
    private final String fileName;
    private final File file;

    public CustomConfig(Plugin plugin, File folder, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(folder, fileName);

        this.createFile();
    }

    public CustomConfig(File file) {
        this.plugin = null;
        this.fileName = null;
        this.file = file;

        try {
            this.load(file);
            this.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public CustomConfig(Plugin plugin, String filename) {
        this(plugin, plugin.getDataFolder(), filename);
    }

    private void createFile() {
        try {
            if (!file.exists()) {
                if (this.plugin.getResource(this.fileName) != null) {
                    this.plugin.saveResource(this.fileName, false);
                } else {
                    this.save(file);
                }

                this.load(file);
                return;
            }

            this.load(file);
            this.save(file);
        } catch (InvalidConfigurationException | IOException exception) {
            exception.fillInStackTrace();
        }
    }

    public void reload() {
        try {
            this.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.fillInStackTrace();
        }
    }

    public void save() {
        try {
            this.save(file);
        } catch (IOException exception) {
            exception.fillInStackTrace();
        }
    }

    public ConfigurationSection getSection(String path) {
        return getConfigurationSection(path);
    }

    public boolean isSection(String path) {
        return isConfigurationSection(path);
    }

}