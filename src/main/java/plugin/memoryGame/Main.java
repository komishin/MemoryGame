package plugin.memoryGame;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.memoryGame.command.MemoryGameCommand;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        MemoryGameCommand memoryGameCommand = new MemoryGameCommand(this);
        Bukkit.getPluginManager().registerEvents( memoryGameCommand, this);
        getCommand("memoryGame").setExecutor( memoryGameCommand);
    }

}
