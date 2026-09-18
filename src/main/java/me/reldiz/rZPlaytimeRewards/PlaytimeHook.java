package me.reldiz.rZPlaytimeRewards.hooks;

import me.reldiz.rZPlaytimeRewards.RZPlaytimeRewards;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class PlaytimeHook {

    private final RZPlaytimeRewards plugin;
    private ProviderType activeProvider;

    public enum ProviderType { CMI, ESSENTIALS, VANILLA }

    public PlaytimeHook(RZPlaytimeRewards plugin) {
        this.plugin = plugin;
        setupProvider();
    }

    private void setupProvider() {
        String configured = plugin.getConfig().getString("provider", "VANILLA").toUpperCase();

        if (configured.equals("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            activeProvider = ProviderType.CMI;
        } else if (configured.equals("ESSENTIALS") && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            activeProvider = ProviderType.ESSENTIALS;
        } else {
            activeProvider = ProviderType.VANILLA;
        }
    }

    public long getPlaytimeSeconds(Player player) {
        switch (activeProvider) {
            case CMI:
                long cmiTime = getCmiPlaytime(player);
                return cmiTime >= 0 ? cmiTime : getVanillaPlaytime(player);
            case ESSENTIALS:
                long essTime = getEssentialsPlaytime(player);
                return essTime >= 0 ? essTime : getVanillaPlaytime(player);
            case VANILLA:
            default:
                return getVanillaPlaytime(player);
        }
    }

    private long getCmiPlaytime(Player player) {
        try {
            Plugin cmiPlugin = Bukkit.getPluginManager().getPlugin("CMI");
            if (cmiPlugin == null) return -1;

            Class<?> cmiClass = Class.forName("com.Zrips.CMI.CMI");
            Method getInstanceMethod = cmiClass.getMethod("getInstance");
            Object cmiInstance = getInstanceMethod.invoke(null);

            Method getPlayerManagerMethod = cmiClass.getMethod("getPlayerManager");
            Object playerManager = getPlayerManagerMethod.invoke(cmiInstance);

            Method getUserMethod = playerManager.getClass().getMethod("getUser", Player.class);
            Object cmiUser = getUserMethod.invoke(playerManager, player);

            if (cmiUser != null) {
                Method getTotalPlayTimeMethod = cmiUser.getClass().getMethod("getTotalPlayTime");
                long ms = (long) getTotalPlayTimeMethod.invoke(cmiUser);
                return ms / 1000;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private long getEssentialsPlaytime(Player player) {
        try {
            Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essPlugin == null) return -1;

            Method getUserMethod = essPlugin.getClass().getMethod("getUser", Player.class);
            Object essUser = getUserMethod.invoke(essPlugin, player);

            if (essUser != null) {
                Method getAmountPlayedMethod = essUser.getClass().getMethod("getAmountPlayed");
                long ms = (long) getAmountPlayedMethod.invoke(essUser);
                return ms / 1000;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private long getVanillaPlaytime(Player player) {
        try {
            return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        } catch (NoSuchFieldError e) {
            return player.getStatistic(Statistic.valueOf("TICKS_PLAYED")) / 20;
        }
    }

    public String getProviderName() {
        return activeProvider.name();
    }
}