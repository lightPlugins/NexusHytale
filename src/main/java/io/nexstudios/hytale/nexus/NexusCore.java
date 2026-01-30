package io.nexstudios.hytale.nexus;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.nexstudios.hytale.nexus.commands.ExampleCommand;
import io.nexstudios.hytale.nexus.configs.NexusFile;
import io.nexstudios.hytale.nexus.configs.NexusFileConfiguration;
import io.nexstudios.hytale.nexus.configs.NexusFileReader;
import io.nexstudios.hytale.nexus.rpg.NexRPG;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class NexusCore extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Getter
    private static NexusCore instance;

    private NexusFile settings;
    private NexusFileReader items;
    private NexRPG rpg;

    public NexusCore(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
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

        rpg = new NexRPG();
        rpg.init();

    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Register Commands ...");
        registerCommands();

        rpg.setup();
    }


    private void registerCommands() {
        CommandManager.get().registerSystemCommand(new ExampleCommand());
    }

}
