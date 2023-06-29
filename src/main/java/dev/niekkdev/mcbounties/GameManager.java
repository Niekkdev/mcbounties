package dev.niekkdev.mcbounties;

import dev.niekkdev.mcbounties.managers.economy.EconomyManager;
import dev.niekkdev.mcbounties.managers.hologram.HologramManager;
import dev.niekkdev.mcbounties.configuration.ConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class GameManager {

    private final McBounties plugin;

    private final EconomyManager economyManager;
    private final ConfigurationManager configurationManager;
    private final HologramManager hologramManager;

    private final HashMap<UUID, TextDisplay> activeTextDisplays = new HashMap<UUID, TextDisplay>();

    private final HashMap<UUID, Long> bountyCoolDowns = new HashMap<UUID, Long>();

    public GameManager(final McBounties plugin) {
        this.plugin = plugin;

        this.economyManager = new EconomyManager(this);
        this.configurationManager = new ConfigurationManager(this);
        this.configurationManager.setupConfig();
        this.hologramManager = new HologramManager(this);
    }

    public McBounties getPlugin() {
        return this.plugin;
    }

    public EconomyManager getEconomyManager() {
        return this.economyManager;
    }

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public HologramManager getHologramManager() {
        return this.hologramManager;
    }

    public HashMap<UUID, TextDisplay> getActiveTextDisplays() {
        return this.activeTextDisplays;
    }

    public TextDisplay getActiveTextDisplay(UUID uuid) {
        return this.activeTextDisplays.get(uuid);
    }

    public void addActiveTextDisplay(UUID uuid, TextDisplay armorStand) {
        this.activeTextDisplays.put(uuid, armorStand);
    }

    public void removeActiveTextDisplay(UUID uuid) {
        this.activeTextDisplays.remove(uuid);
    }

    public boolean checkIfPlayerExists(Player player) {
        Player serverPlayer = Bukkit.getPlayer(player.getUniqueId());

        if (serverPlayer == null) {
            return false;
        }

        return true;
    }

    public String formatAmount(int amount) {
        String currencyConfig = this.configurationManager.getCurrency();

        Locale locale = switch (currencyConfig) {
            case "EUR" -> Locale.GERMANY;
            case "GBP" -> Locale.UK;
            case "SEK" -> new Locale("sv", "SE");
            case "ILS" -> new Locale("he", "IL");
            case "EGP" -> new Locale("ar", "EG");
            case "TRY" -> new Locale("tr", "TR");
            case "USD" -> Locale.US;
            case "AUD" -> new Locale("en", "AU");
            case "CAD" -> Locale.CANADA;
            case "JPY" -> Locale.JAPAN;
            case "INR" -> new Locale("en", "IN");
            case "CNY" -> Locale.CHINA;
            case "RUB" -> new Locale("ru", "RU");
            case "BRL" -> new Locale("pt", "BR");
            default -> Locale.US;
        };

        Currency currency = Currency.getInstance(currencyConfig);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        currencyFormatter.setCurrency(currency);

        return currencyFormatter.format(amount);
    }

    public HashMap<UUID, Long> getBountyCoolDowns() {
        return this.bountyCoolDowns;
    }

    public void addBountyCoolDown(UUID uuid, long time) {
        this.bountyCoolDowns.put(uuid, time);
    }

    public void removeBountyCoolDown(UUID uuid) {
        this.bountyCoolDowns.remove(uuid);
    }

    public boolean checkIfPlayerHasCoolDown(UUID uuid) {
        return this.bountyCoolDowns.containsKey(uuid);
    }
}
