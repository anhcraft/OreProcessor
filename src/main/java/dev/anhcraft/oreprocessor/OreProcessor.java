package dev.anhcraft.oreprocessor;

import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import dev.anhcraft.jvmkit.utils.FileUtil;
import dev.anhcraft.jvmkit.utils.IOUtil;
import dev.anhcraft.oreprocessor.cmd.OreCommand;
import dev.anhcraft.oreprocessor.config.MainConfig;
import dev.anhcraft.oreprocessor.gui.GuiRefreshTask;
import dev.anhcraft.oreprocessor.gui.GuiRegistry;
import dev.anhcraft.oreprocessor.gui.MenuGui;
import dev.anhcraft.oreprocessor.gui.UpgradeGui;
import dev.anhcraft.oreprocessor.handler.ProcessingPlant;
import dev.anhcraft.oreprocessor.storage.PlayerDataManager;
import dev.anhcraft.oreprocessor.util.ConfigHelper;
import dev.anhcraft.palette.listener.GuiEventListener;
import dev.anhcraft.palette.ui.Gui;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class OreProcessor extends JavaPlugin {
    private static OreProcessor INSTANCE;
    public MainConfig mainConfig;
    public ProcessingPlant processingPlant;
    public PlayerDataManager playerDataManager;
    public Economy economy;

    @NotNull
    public static OreProcessor getInstance() {
        return INSTANCE;
    }

    public void debug(@NotNull String format, @NotNull Object... args) {
        if (mainConfig != null && mainConfig.devMode) {
            getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[OreProcessor#Dev] " + String.format(format, args));
        }
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        INSTANCE = this;
        processingPlant = new ProcessingPlant(this);
        playerDataManager = new PlayerDataManager(this);
        reload();

        new GuiRefreshTask().runTaskTimerAsynchronously(this, 0L, 20L);
        getServer().getPluginManager().registerEvents(new GuiEventListener(), this);

        PaperCommandManager pcm = new PaperCommandManager(this);
        pcm.enableUnstableAPI("help");
        pcm.registerCommand(new OreCommand(this));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
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
        getDataFolder().mkdir();
        mainConfig = ConfigHelper.load(MainConfig.class, requestConfig("config.yml"));

        new File(getDataFolder(), "gui").mkdir();
        GuiRegistry.MENU = ConfigHelper.load(MenuGui.class, requestConfig("gui/menu.yml"));
        GuiRegistry.UPGRADE = ConfigHelper.load(UpgradeGui.class, requestConfig("gui/upgrade.yml"));
        GuiRegistry.STORAGE = ConfigHelper.load(Gui.class, requestConfig("gui/storage.yml"));

        processingPlant.refresh();
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
