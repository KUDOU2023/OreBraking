package plugin.orebraking;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.orebraking.command.OreBrakingCommand;

public final class OreBraking extends JavaPlugin {

    @Override
    public void onEnable() {
        OreBrakingCommand oreBrakingCommand = new OreBrakingCommand(this);
        Bukkit.getPluginManager().registerEvents(oreBrakingCommand,this);
        getCommand("spawn").setExecutor(oreBrakingCommand);

    }
}
