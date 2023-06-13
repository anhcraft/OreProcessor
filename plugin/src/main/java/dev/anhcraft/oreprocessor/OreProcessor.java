package dev.anhcraft.oreprocessor;

import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import dev.anhcraft.jvmkit.utils.FileUtil;
import dev.anhcraft.jvmkit.utils.IOUtil;
import dev.anhcraft.oreprocessor.api.OreProcessorApi;
import dev.anhcraft.oreprocessor.cmd.OreCommand;
import dev.anhcraft.oreprocessor.config.MainConfig;
import dev.anhcraft.oreprocessor.config.MessageConfig;
import dev.anhcraft.oreprocessor.config.UpgradeConfig;
import dev.anhcraft.oreprocessor.gui.*;
import dev.anhcraft.oreprocessor.handler.ProcessingPlant;
import dev.anhcraft.oreprocessor.integration.IntegrationManager;
import dev.anhcraft.oreprocessor.storage.PlayerDataManager;
import dev.anhcraft.oreprocessor.util.ConfigHelper;
import dev.anhcraft.palette.listener.GuiEventListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class OreProcessor extends JavaPlugin {
    public static final int LATEST_PLAYER_DATA_VERSION = 1;
    private static OreProcessor INSTANCE;
    private static OreProcessorApiImpl API;
    public IntegrationManager integrationManager;
    public PlayerDataManager playerDataManager;
    private ProcessingPlant processingPlant;
    public Economy economy;
    public MessageConfig messageConfig;
    MainConfig mainConfig;
    UpgradeConfig upgradeConfig;

    @NotNull
    public static OreProcessor getInstance() {
        return INSTANCE;
    }

    public static OreProcessorApi getApi() {
        return API;
    }

    public void debug(@NotNull String format, @NotNull Object... args) {
        if (mainConfig != null && mainConfig.devMode) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[OreProcessor#Dev] " + String.format(format, args));
        }
    }

    public void msg(CommandSender sender, String str) {
        if (str == null) {
            sender.sendMessage(ChatColor.RED + "<Missing message>");
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageConfig.prefix + str));
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        INSTANCE = this;
        API = new OreProcessorApiImpl(this);
        playerDataManager = new PlayerDataManager(this);
        processingPlant = new ProcessingPlant(this);
        integrationManager = new IntegrationManager(this);

        reload();

        getServer().getPluginManager().registerEvents(new GuiEventListener(), this);

        PaperCommandManager pcm = new PaperCommandManager(this);
        pcm.enableUnstableAPI("help");
        pcm.registerCommand(new OreCommand(this));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        playerDataManager.terminate();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public void reload() {
        getServer().getScheduler().cancelTasks(this);

        getDataFolder().mkdir();
        mainConfig = ConfigHelper.load(MainConfig.class, requestConfig("config.yml"));
        messageConfig = ConfigHelper.load(MessageConfig.class, requestConfig("messages.yml"));
        upgradeConfig = ConfigHelper.load(UpgradeConfig.class, requestConfig("upgrades.yml"));

        new File(getDataFolder(), "gui").mkdir();
        GuiRegistry.MENU = ConfigHelper.load(MenuGui.class, requestConfig("gui/menu.yml"));
        GuiRegistry.UPGRADE = ConfigHelper.load(UpgradeGui.class, requestConfig("gui/upgrade.yml"));
        GuiRegistry.STORAGE = ConfigHelper.load(StorageGui.class, requestConfig("gui/storage.yml"));

        processingPlant.reload();
        playerDataManager.reload();
        API.reload();

        new GuiRefreshTask().runTaskTimer(this, 0L, 10L);
    }

    public YamlConfiguration requestConfig(String path) {
        File f = new File(getDataFolder(), path);
        Preconditions.checkArgument(f.getParentFile().exists());

        if (!f.exists()) {
            try {
                FileUtil.write(f, IOUtil.readResource(OreProcessor.class, "/config/" + path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(f);
    }
}
