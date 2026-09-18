package me.reldiz.rZPlaytimeRewards;

import me.reldiz.rZPlaytimeRewards.commands.PlaytimeCommand;
import me.reldiz.rZPlaytimeRewards.hooks.PlaytimeHook;
import me.reldiz.rZPlaytimeRewards.listeners.GUIListener;
import me.reldiz.rZPlaytimeRewards.managers.DataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RZPlaytimeRewards extends JavaPlugin {

    private static RZPlaytimeRewards instance;
    private PlaytimeHook playtimeHook;
    private DataManager dataManager;

    private File guiFile, rewardsFile;
    private FileConfiguration guiConfig, rewardsConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        createCustomConfigs();

        this.playtimeHook = new PlaytimeHook(this);
        this.dataManager = new DataManager(this);

        if (getCommand("playtime") != null) {
            getCommand("playtime").setExecutor(new PlaytimeCommand(this));
        }

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        getLogger().info("RZ-PlaytimeRewards enabled! Hooked Provider: " + playtimeHook.getProviderName());
    }

    public static RZPlaytimeRewards getInstance() {
        return instance;
    }

    public PlaytimeHook getPlaytimeHook() {
        return playtimeHook;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public void reloadPluginConfigs() {
        reloadConfig();
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    }

    private void createCustomConfigs() {
        guiFile = new File(getDataFolder(), "gui.yml");
        rewardsFile = new File(getDataFolder(), "rewards.yml");

        if (!guiFile.exists()) saveResource("gui.yml", false);
        if (!rewardsFile.exists()) saveResource("rewards.yml", false);

        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    }
}