package me.reldiz.rZPlaytimeRewards.listeners;

import me.reldiz.rZPlaytimeRewards.RZPlaytimeRewards;
import me.reldiz.rZPlaytimeRewards.gui.RewardGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIListener implements Listener {

    private final RZPlaytimeRewards plugin;

    public GUIListener(RZPlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String rawTitle = plugin.getGuiConfig().getString("gui.title", "&8Playtime Rewards");
        String colorTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);

        if (!event.getView().getTitle().startsWith(colorTitle)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        int slot = event.getSlot();
        int currentPage = parsePage(event.getView().getTitle());

        // Previous page click
        if (slot == plugin.getGuiConfig().getInt("gui.items.previous-page.slot", 18)) {
            if (currentPage > 1) {
                playSound(player, plugin.getConfig().getString("sounds.page-change", "ITEM_BOOK_PAGE_TURN"));
                new RewardGUI(plugin, player, currentPage - 1).open();
            }
            return;
        }

        // Next page click (Tikrina, ar išvis yra sekančių prizų)
        if (slot == plugin.getGuiConfig().getInt("gui.items.next-page.slot", 26)) {
            ConfigurationSection rewardsSec = plugin.getRewardsConfig().getConfigurationSection("rewards");
            int totalRewards = rewardsSec != null ? rewardsSec.getKeys(false).size() : 0;
            int maxPerPage = plugin.getGuiConfig().getIntegerList("gui.reward-slots").size();

            if (currentPage * maxPerPage < totalRewards) {
                playSound(player, plugin.getConfig().getString("sounds.page-change", "ITEM_BOOK_PAGE_TURN"));
                new RewardGUI(plugin, player, currentPage + 1).open();
            }
            return;
        }

        // Reward slot click
        List<Integer> rewardSlots = plugin.getGuiConfig().getIntegerList("gui.reward-slots");
        if (rewardSlots.contains(slot)) {
            int indexInPage = rewardSlots.indexOf(slot);
            int itemsPerPage = rewardSlots.size();
            int targetIndex = ((currentPage - 1) * itemsPerPage) + indexInPage;

            ConfigurationSection rewardsSec = plugin.getRewardsConfig().getConfigurationSection("rewards");
            if (rewardsSec == null) return;

            List<String> keys = new ArrayList<>(rewardsSec.getKeys(false));
            if (targetIndex >= keys.size()) return;

            String rewardKey = keys.get(targetIndex);
            ConfigurationSection reward = rewardsSec.getConfigurationSection(rewardKey);

            if (plugin.getDataManager().hasClaimed(player.getUniqueId(), rewardKey)) {
                sendMessage(player, plugin.getConfig().getString("messages.already-claimed"));
                playSound(player, plugin.getConfig().getString("sounds.error", "ENTITY_VILLAGER_NO"));
                return;
            }

            long requiredTime = reward.getLong("playtime-required");
            long currentPlaytime = plugin.getPlaytimeHook().getPlaytimeSeconds(player);

            if (currentPlaytime < requiredTime) {
                String msg = plugin.getConfig().getString("messages.reward-locked")
                        .replace("%required%", formatTime(requiredTime))
                        .replace("%current%", formatTime(currentPlaytime));
                sendMessage(player, msg);
                playSound(player, plugin.getConfig().getString("sounds.error", "ENTITY_VILLAGER_NO"));
                return;
            }

            // Save claim status
            plugin.getDataManager().addClaim(player.getUniqueId(), rewardKey);

            // Execute configured commands
            executeRewardCommands(player, reward.getStringList("commands"));

            String msg = plugin.getConfig().getString("messages.reward-claimed")
                    .replace("%reward%", reward.getString("display-name", rewardKey));
            sendMessage(player, msg);
            playSound(player, plugin.getConfig().getString("sounds.reward-claimed", "ENTITY_PLAYER_LEVELUP"));

            // Refresh menu
            new RewardGUI(plugin, player, currentPage).open();
        }
    }

    private void executeRewardCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;

        for (String line : commands) {
            line = line.replace("%player%", player.getName());

            if (line.startsWith("[player] ")) {
                String cmd = line.substring(9).trim();
                player.performCommand(cmd);
            } else if (line.startsWith("[message] ")) {
                String msg = line.substring(10).trim();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            } else if (line.startsWith("[broadcast] ")) {
                String broadcast = line.substring(12).trim();
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
            } else {
                String cmd = line.startsWith("[console] ") ? line.substring(10).trim() : line.trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }

    private int parsePage(String title) {
        try {
            String[] parts = title.split(" - Page ");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            String soundKey = soundName.toLowerCase(Locale.ROOT).replace("_", ".");
            player.playSound(player.getLocation(), soundKey, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    private void sendMessage(Player player, String message) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}