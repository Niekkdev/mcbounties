package dev.niekk.mcbounties;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;

import dev.niekk.mcbounties.commands.GameCommands;
import dev.niekk.mcbounties.events.Events;

import dev.niekk.mcbounties.utils.Metrics;
import dev.niekk.mcbounties.utils.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;

public class McBounties extends JavaPlugin {

    private AnnotationParser<CommandSender> annotationParser = null;

    private GameManager gameManager;

    @Override
    public void onEnable() {

        if (!this.initCommandManager()) {
            this.getLogger().severe("Failed to initialize the command manager");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            getLogger().warning("No economy plugin found. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!this.isValidSpigotVersion()) {
            this.getLogger().warning("This plugin requires Minecraft 1.19.4 or 1.20");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new UpdateChecker(this, 109887).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().warning("There is a new update available.");
            }
        });

        int pluginId = 18527;
        new Metrics(this, pluginId);

        this.gameManager = new GameManager(this);

        this.registerCommands();

        this.registerEvents();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("McBounties has been disabled");
    }

    private void registerCommands() {
        this.annotationParser.parse(new GameCommands(this.gameManager));
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new Events(this.gameManager), this);
    }

    private boolean isValidSpigotVersion() {

        String packageName = this.getServer().getClass().getPackage().getName();

        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        return version.equals("v1_20_R1") || version.equals("v1_19_R3");
    }

    private boolean initCommandManager() {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>>
                executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        final PaperCommandManager<CommandSender> commandManager;
        try {
            commandManager =
                    new PaperCommandManager<>(
                            /* Owning plugin */ this,
                            /* Coordinator function */ executionCoordinatorFunction,
                            /* Command Sender -> C */ mapperFunction,
                            /* C -> Command Sender */ mapperFunction);
        } catch (final Exception e) {
            return false;
        }

        // Register Brigadier mappings
        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier();
        }

        // Register asynchronous completions
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        final CommandConfirmationManager<CommandSender> confirmationManager =
                new CommandConfirmationManager<>(
                        /* Timeout */ 30L,
                        /* Timeout unit */ TimeUnit.SECONDS,
                        /* Action when confirmation is required */ context ->
                        context
                                .getCommandContext()
                                .getSender()
                                .sendMessage(Component.text(ChatColor.RED + "Confirmation required. Confirm using /game confirm.")),
                        /* Action when no confirmation is pending */ sender ->
                        sender.sendMessage(Component.text(ChatColor.RED + "You don't have any pending commands.")));

        // Register the confirmation processor.
        // This will enable confirmations for commands that require it
        confirmationManager.registerConfirmationProcessor(commandManager);

        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        final Function<ParserParameters, CommandMeta> commandMetaFunction =
                p ->
                        CommandMeta.simple()
                                // This will allow you to decorate commands with descriptions
                                .with(
                                        CommandMeta.DESCRIPTION,
                                        p.get(StandardParameters.DESCRIPTION, "No description"))
                                .build();

        this.annotationParser =
                new AnnotationParser<>(
                        /* Manager */ commandManager,
                        /* Command sender type */ CommandSender.class,
                        /* Mapper for command meta instances */ commandMetaFunction);

        // Parse all @CommandMethod-annotated methods
        this.annotationParser.parse(this);

        // Parse all @CommandContainer-annotated classes
        try {
            this.annotationParser.parseContainers();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // Add the confirmation command
//        commandManager.command(
//                commandManager
//                        .commandBuilder("game")
//                        .literal("confirm")
//                        .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
//                        .handler(confirmationManager.createConfirmationExecutionHandler()));

        // Add exception handler
        commandManager.registerExceptionHandler(
                CommandExecutionException.class,
                (commandSender, e) -> {
                    if (e.getCause() instanceof CommandException) {
                        return;
                    }

                    commandSender.sendMessage(Component.text(ChatColor.RED + " An error occurred while executing the command. See console for details."));
                    commandManager
                            .getOwningPlugin()
                            .getLogger()
                            .log(Level.SEVERE, "Exception executing command handler", e.getCause().getCause());
                    e.printStackTrace();
                });

        return true;
    }
}
