package dev.niekkdev.mcbounties.events;

import dev.niekkdev.mcbounties.GameManager;
import dev.niekkdev.mcbounties.managers.economy.EconomyManager;
import dev.niekkdev.mcbounties.managers.hologram.HologramManager;
import dev.niekkdev.mcbounties.configuration.ConfigurationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Events implements Listener {

    private final GameManager gameManager;
    private final ConfigurationManager configurationManager;
    private final EconomyManager economyManager;
    private final HologramManager hologramManager;

    public Events(GameManager gameManager) {
        this.gameManager = gameManager;
        this.configurationManager = this.gameManager.getConfigurationManager();
        this.economyManager = this.gameManager.getEconomyManager();
        this.hologramManager = this.gameManager.getHologramManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        Player killer = event.getEntity().getKiller();

        int amount = this.configurationManager.getPlayerBounty(victim);

        TextDisplay textDisplay = this.gameManager.getActiveTextDisplay(victim.getUniqueId());

        if (!victim.equals(killer)) {

            if (killer == null) {
                textDisplay.remove();
                return;
            }

            Location killerLocation = killer.getLocation();
            Location victimLocation = victim.getLocation();

            if (textDisplay == null) {
                return;
            }

            this.reset(victim, textDisplay);

            this.economyManager.depositPlayer(killer, amount);

            String formattedPlayerAmount = this.gameManager.formatAmount(amount);

            killer.playSound(killerLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1 ,2);
            killer.sendMessage(Component.text(this.configurationManager.formatMessageWithPrefix("messages.player_received_bounty_amount").replace("%amount%", formattedPlayerAmount).replace("%player%", victim.getName())));

            victim.playSound(victimLocation, Sound.ENTITY_BEE_DEATH, 1, 2);
            victim.sendMessage(Component.text(this.configurationManager.formatMessageWithPrefix("messages.victim_died")));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        int bounty = this.configurationManager.getPlayerBounty(player);

        if (bounty > 0) {
            this.hologramManager.init(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int bounty = this.configurationManager.getPlayerBounty(player);

        if (bounty > 0) {
            this.hologramManager.init(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        TextDisplay textDisplay = this.gameManager.getActiveTextDisplay(player.getUniqueId());

        if (this.configurationManager.getPlayerBounty(player) > 0 && textDisplay != null && this.gameManager.getActiveTextDisplay(player.getUniqueId()) != null) {
            textDisplay.remove();
        }
    }

    private void reset(Player player, TextDisplay textDisplay) {
        this.gameManager.removeActiveTextDisplay(player.getUniqueId());
        this.configurationManager.removePlayerBounty(player);
        this.configurationManager.reloadConfig();
        textDisplay.remove();
    }
}
