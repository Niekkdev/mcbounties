package dev.niekk.mcbounties.managers.hologram;

import dev.niekk.mcbounties.GameManager;
import dev.niekk.mcbounties.configuration.ConfigurationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramManager {

    private final GameManager gameManager;
    private final ConfigurationManager configurationManager;

    public HologramManager(GameManager gameManager) {
        this.gameManager = gameManager;

        this.configurationManager = this.gameManager.getConfigurationManager();
    }

    public void init(Player player) {

        if (!this.gameManager.checkIfPlayerExists(player)) {
            player.sendMessage(Component.text(ChatColor.RED + "Could not find player " + player.getName()));
            return;
        }

        TextDisplay textDisplay = this.generateTextDisplay(player);

        this.gameManager.addActiveTextDisplay(player.getUniqueId(), textDisplay);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (textDisplay.isValid() && player.isValid()) {
                    if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            target.hideEntity(gameManager.getPlugin(), textDisplay);
                        }
                    } else {
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            target.showEntity(gameManager.getPlugin(), textDisplay);
                        }
                    }
                    Location targetLoc = player.getLocation().add(0, 2.3, 0);
                    textDisplay.teleport(targetLoc);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(this.gameManager.getPlugin(), 0, 3);
    }

    private TextDisplay generateTextDisplay(Player player) {

        int amount = this.configurationManager.getPlayerBounty(player);

        String formattedPlayerAmount = this.gameManager.formatAmount(amount);

        TextDisplay textDisplay = player.getWorld().spawn(player.getLocation(), TextDisplay.class);

        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.text(Component.text("Bounty: " + ChatColor.YELLOW + formattedPlayerAmount));

        return textDisplay;
    }
}
