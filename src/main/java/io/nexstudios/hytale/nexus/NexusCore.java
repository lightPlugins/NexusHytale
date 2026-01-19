package io.nexstudios.hytale.nexus;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.nexstudios.hytale.nexus.configs.NexusFile;
import io.nexstudios.hytale.nexus.configs.NexusFileConfiguration;
import io.nexstudios.hytale.nexus.configs.NexusFileReader;

import javax.annotation.Nonnull;

public class NexusCore extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private NexusFile settings;
    private NexusFileReader items;

    public NexusCore(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Loading NexusCore ... " + getDataDirectory().toAbsolutePath());
        LOGGER.atInfo().log("Initializing Configs ...");

        settings = new NexusFile(getDataDirectory(), "settings.yml", "settings.yml", true, this.getClassLoader(), "Settings.yml");
        settings.reload();
        LOGGER.atInfo().log("Configs loaded! ->" + settings.getConfig().getString("new", "Unknown"));

        items = new NexusFileReader(getDataDirectory().toAbsolutePath(), "items");
        LOGGER.atInfo().log("Found " + items.getFiles().size() + " Item Files!");

        for (NexusFileConfiguration config : items.getNexusFiles()) {
            LOGGER.atInfo().log("Processing Item File: " + config.getFileName());
        }


    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Register Commands ...");
        registerCommands();
    }


    private void registerCommands() {
        CommandManager.get().registerSystemCommand(new ExampleCommand());
    }

}
