package dev.niekk.mcbounties.configuration;

import dev.niekk.mcbounties.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ConfigurationManager {

    private final GameManager gameManager;
    private FileConfiguration config;

    public ConfigurationManager(GameManager gameManager) {
        this.gameManager = gameManager;

        this.config = gameManager.getPlugin().getConfig();
    }

    public void setupConfig() {
        config.options().copyDefaults(true);
        gameManager.getPlugin().saveDefaultConfig();
    }

    public void reloadConfig() {
        gameManager.getPlugin().reloadConfig();

        this.config = gameManager.getPlugin().getConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public String formatMessageWithPrefix(String messageConfigPath) {
        return ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + " " + this.config.getString(messageConfigPath));
    }

    public void setPlayerBounty(Player player, int amount) {
        this.config.set("users." + player.getUniqueId() + ".bounty", amount);
        gameManager.getPlugin().saveConfig();
    }

    public void removePlayerBounty(Player player) {
        this.config.set("users." + player.getUniqueId(), null);
        gameManager.getPlugin().saveConfig();
    }

    public int getPlayerBounty(Player player) {
        if (this.config.contains("users." + player.getUniqueId() + ".bounty")) {
            return this.config.getInt("users." + player.getUniqueId() + ".bounty");
        } else {
            return 0;
        }
    }

    public Player getBountyTarget(Player player) {
        if (this.config.contains("users." + player.getUniqueId())) {
            return Bukkit.getPlayer(player.getUniqueId());
        } else {
            return null;
        }
    }

    public long getBountyConfigCoolDownAmount() {
        return this.config.getLong("bounty_command_cool_down");
    }

    public String getCurrency() {
        return this.config.getString("currency");
    }
}
