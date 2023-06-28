package dev.niekk.mcbounties.managers.economy;

import dev.niekk.mcbounties.GameManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final GameManager gameManager;
    private Economy economy;

    public EconomyManager(GameManager gameManager) {
        this.gameManager = gameManager;

        this.initVaultApi();
    }

    private void initVaultApi() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.gameManager.getPlugin().getLogger().warning("Vault not found on the server. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this.gameManager.getPlugin());
            return;
        }

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            this.gameManager.getPlugin().getLogger().warning("No economy plugin found. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this.gameManager.getPlugin());
            return;
        }

        this.economy = registeredServiceProvider.getProvider();
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public void withdrawPlayer(Player player, double amount) {
        this.economy.withdrawPlayer(player, amount);
    }

    public void depositPlayer(Player player, double amount) {
        this.economy.depositPlayer(player, amount);
    }

    public double getBalance(Player player) {
        return this.economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        return this.economy.has(player, amount);
    }
}
