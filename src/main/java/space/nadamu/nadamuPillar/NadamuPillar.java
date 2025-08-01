package space.nadamu.nadamuPillar;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public final class NadamuPillar extends JavaPlugin {

    private static Logger logger;
    private static File dataFolder;

    @Override
    public void onEnable() {
        logger = super.getLogger();
        dataFolder = super.getDataFolder();
        Config.init(dataFolder.toPath(), this.getClass().getClassLoader().getResourceAsStream("config.toml"));
        Objects.requireNonNull(super.getServer().getPluginCommand("nadamupillars")).setExecutor(new Commands());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
