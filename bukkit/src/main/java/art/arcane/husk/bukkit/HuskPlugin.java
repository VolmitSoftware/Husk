package art.arcane.husk.bukkit;

import art.arcane.husk.core.BuildIdentity;
import art.arcane.husk.core.HuskRuntime;
import art.arcane.husk.core.HuskSplash;
import art.arcane.husk.core.RuntimeEnvironment;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuskPlugin extends JavaPlugin {
    private volatile HuskRuntime runtime;

    @Override
    public void onEnable() {
        try {
            RuntimeEnvironment environment = RuntimeEnvironment.bukkit(
                    getServer().getBukkitVersion(), getDataFolder().toPath());
            runtime = new HuskRuntime(environment);
            runtime.start();
            printSplash(HuskSplash.render(environment, getServer().getVersion(), true));
        } catch (IOException | RuntimeException | LinkageError exception) {
            getLogger().log(Level.SEVERE, "Failed to start " + BuildIdentity.NAME, exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        HuskRuntime activeRuntime = runtime;
        runtime = null;
        if (activeRuntime == null) {
            return;
        }
        try {
            activeRuntime.close();
        } catch (RuntimeException | LinkageError exception) {
            getLogger().log(Level.SEVERE, "Failed to stop " + BuildIdentity.NAME, exception);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (arguments.length > 1 || (arguments.length == 1 && !arguments[0].equalsIgnoreCase("status"))) {
            return false;
        }
        HuskRuntime activeRuntime = runtime;
        sender.sendMessage(activeRuntime == null ? BuildIdentity.NAME + " is not running." : activeRuntime.status());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1 && "status".startsWith(arguments[0].toLowerCase(Locale.ROOT))) {
            return List.of("status");
        }
        return List.of();
    }

    private void printSplash(String splash) {
        StringBuilder colored = new StringBuilder();
        ChatColor previous = null;
        for (int index = 0; index < splash.length(); index++) {
            char glyph = splash.charAt(index);
            ChatColor color;
            if (glyph == '█') {
                color = ChatColor.DARK_GRAY;
            } else if (glyph >= '\u2500' && glyph <= '\u257f') {
                color = ChatColor.GOLD;
            } else {
                color = ChatColor.GRAY;
            }
            if (color != previous) {
                colored.append(color);
                previous = color;
            }
            colored.append(glyph);
        }
        getServer().getConsoleSender().sendMessage(colored.toString()
                .replace("Startup: READY", "Startup: " + ChatColor.GREEN + "READY" + ChatColor.GRAY)
                + ChatColor.RESET);
    }
}
