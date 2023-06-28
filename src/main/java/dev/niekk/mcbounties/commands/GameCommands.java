package dev.niekk.mcbounties.commands;

import cloud.commandframework.annotations.*;
import cloud.commandframework.annotations.specifier.Range;
import dev.niekk.mcbounties.GameManager;
import dev.niekk.mcbounties.configuration.ConfigurationManager;
import dev.niekk.mcbounties.managers.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameCommands {

    private final GameManager gameManager;
    private final ConfigurationManager configurationManager;
    private final EconomyManager economyManager;

    public GameCommands(GameManager gameManager) {
        this.gameManager = gameManager;
        this.configurationManager = this.gameManager.getConfigurationManager();
        this.economyManager = this.gameManager.getEconomyManager();
    }

    @CommandMethod("bounty set <target> <amount>")
    @CommandDescription("Set a bounty on a player")
    private void bountyShortcutCommand(
            final CommandSender commandSender,
            final @Argument("target") @NotNull Player target,
            final @Argument("amount") @NotNull @Range(min = "0", max = "100000") Integer amount) {

        bountyCommand(commandSender, target, amount);
    }

    @CommandMethod("mcbounties bounty set <target> <amount>")
    @CommandDescription("Set a bounty on a player")
    private void bountyCommand(
            final CommandSender commandSender,
            final @Argument("target") @NotNull Player target,
            final @Argument("amount") @NotNull @Range(min = "0", max = "100000") Integer amount) {

        Player player = (Player) commandSender;

        if (this.gameManager.checkIfPlayerHasCoolDown(player.getUniqueId())) {
            long coolDownConfig = this.configurationManager.getBountyConfigCoolDownAmount();
            long coolDownPlayer = this.gameManager.getBountyCoolDowns().get(player.getUniqueId());
            final long secondsLeft = ( ( coolDownPlayer) / 1000 ) + coolDownConfig - ( System.currentTimeMillis() / 1000 );

            player.sendMessage(this.configurationManager.formatMessageWithPrefix("messages.bounty_cool_down").replace("%time%", Long.toString(secondsLeft)));
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(this.configurationManager.formatMessageWithPrefix("messages.bounty_on_yourself"));
            return;
        }

        if (this.configurationManager.getPlayerBounty(target) != 0) {
            player.sendMessage(this.configurationManager.formatMessageWithPrefix("messages.player_already_bountied"));
            return;
        }

        if (!this.economyManager.has(player, amount)) {
            player.sendMessage(this.configurationManager.formatMessageWithPrefix("messages.no_money"));
            return;
        }

        this.configurationManager.setPlayerBounty(target, amount);
        this.gameManager.getHologramManager().init(target);

        int playerBountyAmount = this.configurationManager.getPlayerBounty(target);

        String formattedPlayerBountyAmount = this.gameManager.formatAmount(playerBountyAmount);

        this.economyManager.withdrawPlayer(player, playerBountyAmount);

        this.gameManager.addBountyCoolDown(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(Component.text(this.configurationManager.formatMessageWithPrefix("messages.withdrawn_set_amount").replace("%amount%", formattedPlayerBountyAmount)));
        player.sendMessage(Component.text(this.configurationManager.formatMessageWithPrefix("messages.bounty_set_player_amount").replace("%amount%", formattedPlayerBountyAmount).replace("%player%", target.getName())));
        Bukkit.broadcast(Component.text(this.configurationManager.formatMessageWithPrefix("messages.player_bounty_amount").replace("%amount%", formattedPlayerBountyAmount).replace("%player%", target.getName())));
    }

    @CommandMethod("mcbounties info")
    @CommandDescription("Get info about the plugin.")
    @CommandPermission("mcbounties.info")
    private void bountyCommand(CommandSender commandSender) {

        Player player = (Player) commandSender;

        player.sendMessage(Component.text(ChatColor.GOLD + "Made by " + ChatColor.AQUA + "Codr & Niek" + ChatColor.GOLD + " !"));

        final TextComponent text = Component.text("You can download this plugin ")
                .append(
                        Component
                                .text()
                                .clickEvent(
                                        ClickEvent
                                                .openUrl("https://www.spigotmc.org/resources/mcbounties.109887/"))
                                .content("here")
                                .hoverEvent(HoverEvent
                                        .hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to open the plugin page !")))
                                .color(NamedTextColor.AQUA)
                                .decoration(TextDecoration.UNDERLINED, true)
                                .build());

        player.sendMessage(text);
    }

    @CommandMethod("mcbounties reload")
    @CommandDescription("reload the plugin")
    @CommandPermission("mcbounties.reload")
    private void reloadCommand(CommandSender commandSender) {

            Player player = (Player) commandSender;

            this.configurationManager.reloadConfig();

            player.sendMessage(Component.text(ChatColor.GREEN + "Reloaded the config!"));
    }
}
