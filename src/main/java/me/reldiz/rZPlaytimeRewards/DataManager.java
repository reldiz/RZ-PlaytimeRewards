package me.reldiz.rZPlaytimeRewards.managers;

import me.reldiz.rZPlaytimeRewards.RZPlaytimeRewards;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final RZPlaytimeRewards plugin;
    private File file;
    private FileConfiguration data;

    public DataManager(RZPlaytimeRewards plugin) {
        this.plugin = plugin;
        createDataFile();
    }

    private void createDataFile() {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public boolean hasClaimed(UUID uuid, String rewardKey) {
        List<String> claimed = data.getStringList(uuid.toString());
        return claimed.contains(rewardKey);
    }

    public void addClaim(UUID uuid, String rewardKey) {
        List<String> claimed = data.getStringList(uuid.toString());
        if (!claimed.contains(rewardKey)) {
            claimed.add(rewardKey);
            data.set(uuid.toString(), claimed);
            save();
        }
    }

    public List<String> getClaimedRewards(UUID uuid) {
        return data.getStringList(uuid.toString());
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml!");
        }
    }
}