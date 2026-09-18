package me.reldiz.rZPlaytimeRewards.gui;

import me.reldiz.rZPlaytimeRewards.RZPlaytimeRewards;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RewardGUI {

    private final RZPlaytimeRewards plugin;
    private final Player player;
    private final int page;

    public RewardGUI(RZPlaytimeRewards plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
    }

    public void open() {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getGuiConfig().getString("gui.title", "&8Playtime Rewards"));
        int size = plugin.getGuiConfig().getInt("gui.rows", 3) * 9;

        Inventory gui = Bukkit.createInventory(null, size, title + " - Page " + page);

        renderFillers(gui);
        renderStatsItem(gui);
        renderInfoBook(gui);
        renderRewards(gui);
        renderNavigation(gui);

        player.openInventory(gui);
    }

    private void renderFillers(Inventory gui) {
        ConfigurationSection section = plugin.getGuiConfig().getConfigurationSection("gui.items.filler");
        if (section == null) return;

        ItemStack item = new ItemStack(Material.valueOf(section.getString("material", "GRAY_STAINED_GLASS_PANE")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(section.getString("name", " ")));
        item.setItemMeta(meta);

        for (int slot : section.getIntegerList("slots")) {
            if (slot >= 0 && slot < gui.getSize()) {
                gui.setItem(slot, item);
            }
        }
    }

    private void renderStatsItem(Inventory gui) {
        ConfigurationSection section = plugin.getGuiConfig().getConfigurationSection("gui.items.stats-head");
        if (section == null) return;

        ItemStack item = new ItemStack(Material.valueOf(section.getString("material", "HOPPER_MINECART")));
        ItemMeta meta = item.getItemMeta();

        long playtimeSec = plugin.getPlaytimeHook().getPlaytimeSeconds(player);

        meta.setDisplayName(colorize(section.getString("name")));
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(colorize(line
                    .replace("%playtime_formatted%", formatTime(playtimeSec))
                    .replace("%claimable_count%", String.valueOf(getClaimableCount(playtimeSec)))
                    .replace("%claimed_count%", String.valueOf(getClaimedCount()))
            ));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        gui.setItem(section.getInt("slot", 0), item);
    }

    private void renderInfoBook(Inventory gui) {
        ConfigurationSection section = plugin.getGuiConfig().getConfigurationSection("gui.items.info-book");
        if (section == null) return;

        ItemStack item = new ItemStack(Material.valueOf(section.getString("material", "BOOK")));
        ItemMeta meta = item.getItemMeta();

        long playtimeSec = plugin.getPlaytimeHook().getPlaytimeSeconds(player);

        meta.setDisplayName(colorize(section.getString("name")));
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(colorize(line.replace("%playtime_formatted%", formatTime(playtimeSec))));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        gui.setItem(section.getInt("slot", 22), item);
    }

    private void renderRewards(Inventory gui) {
        List<Integer> slots = plugin.getGuiConfig().getIntegerList("gui.reward-slots");
        ConfigurationSection rewardsSec = plugin.getRewardsConfig().getConfigurationSection("rewards");
        if (rewardsSec == null) return;

        List<String> keys = new ArrayList<>(rewardsSec.getKeys(false));
        int itemsPerPage = slots.size();
        int startIndex = (page - 1) * itemsPerPage;

        long currentPlaytime = plugin.getPlaytimeHook().getPlaytimeSeconds(player);

        for (int i = 0; i < itemsPerPage; i++) {
            int targetIndex = startIndex + i;
            if (targetIndex >= keys.size()) break;

            String rewardKey = keys.get(targetIndex);
            ConfigurationSection reward = rewardsSec.getConfigurationSection(rewardKey);
            long requiredTime = reward.getLong("playtime-required");
            boolean claimed = plugin.getDataManager().hasClaimed(player.getUniqueId(), rewardKey);

            String state = claimed ? "claimed" : (currentPlaytime >= requiredTime ? "claimable" : "locked");
            ConfigurationSection stateSec = plugin.getGuiConfig().getConfigurationSection("gui.display-states." + state);

            ItemStack item = new ItemStack(Material.valueOf(stateSec.getString("material", "CHEST_MINECART")));
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(colorize(stateSec.getString("name").replace("%reward_name%", reward.getString("display-name"))));

            List<String> descriptionLines = reward.getStringList("description");

            List<String> lore = new ArrayList<>();
            for (String l : stateSec.getStringList("lore")) {
                if (l.contains("%description%")) {
                    for (String descLine : descriptionLines) {
                        lore.add(colorize(descLine));
                    }
                } else {
                    lore.add(colorize(l
                            .replace("%required_time%", formatTime(requiredTime))
                            .replace("%current_time%", formatTime(currentPlaytime))
                    ));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(slots.get(i), item);
        }
    }

    private void renderNavigation(Inventory gui) {
        if (page > 1) {
            setItem(gui, "gui.items.previous-page", page - 1);
        }

        ConfigurationSection rewardsSec = plugin.getRewardsConfig().getConfigurationSection("rewards");
        int totalRewards = rewardsSec != null ? rewardsSec.getKeys(false).size() : 0;
        int maxPerPage = plugin.getGuiConfig().getIntegerList("gui.reward-slots").size();

        if (page * maxPerPage < totalRewards) {
            setItem(gui, "gui.items.next-page", page + 1);
        }
    }

    private void setItem(Inventory gui, String path, int targetPage) {
        ConfigurationSection sec = plugin.getGuiConfig().getConfigurationSection(path);
        if (sec == null) return;

        ItemStack item = new ItemStack(Material.valueOf(sec.getString("material", "ARROW")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(sec.getString("name")));

        List<String> lore = new ArrayList<>();
        for (String line : sec.getStringList("lore")) {
            lore.add(colorize(line.replace("%page%", String.valueOf(targetPage))));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        gui.setItem(sec.getInt("slot"), item);
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }

    private int getClaimableCount(long currentPlaytime) {
        ConfigurationSection sec = plugin.getRewardsConfig().getConfigurationSection("rewards");
        if (sec == null) return 0;
        int count = 0;
        for (String key : sec.getKeys(false)) {
            long req = sec.getLong(key + ".playtime-required");
            if (currentPlaytime >= req && !plugin.getDataManager().hasClaimed(player.getUniqueId(), key)) {
                count++;
            }
        }
        return count;
    }

    private int getClaimedCount() {
        return plugin.getDataManager().getClaimedRewards(player.getUniqueId()).size();
    }

    private String colorize(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}