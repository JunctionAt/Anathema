package at.junction.anathema;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.command.*;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import at.junction.api.*;

import org.json.*;

public class Anathema extends JavaPlugin implements Listener
{
    String server;
    JunctionAPI api;
    LookupContextStore contextStore;
    final HashMap<String,String> message = new HashMap<String,String>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        server = getConfig().getString("server");
        for (final String key : getConfig().getKeys(true)) {
            if (key.regionMatches(0, "message.", 0, 8)) {
                message.put(key.substring(8), getConfig().getString(key));
            }
        }
        try {
            api = new JunctionClient(
                getConfig().getString("api.base"),
                getConfig().getString("api.username"),
                getConfig().getString("api.password"));
        } catch (final Exception e) {
            getPluginLoader().disablePlugin(this);
            throw new RuntimeException(e);
        }
        contextStore = new LookupContextStore();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Anathema was enabled.");
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((JavaPlugin)this);
        message.clear();
        api = null;
        getLogger().info("Anathema was disabled.");
    }

    enum Command
    {    
        a(new AnathemaCommandExecutor());

        final CommandExecutor executor;

        Command(final CommandExecutor executor) {
            this.executor = executor;
        }
    };
    
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        CommandExecutor executor;
        try {
            executor = Command.valueOf(command.getName().toLowerCase()).executor;
        } catch (final IllegalArgumentException e) {
            return false;
        }
        return executor.onCommand(sender, command, label, args);
    }
    
    void sendMessage(final CommandSender sender, final String message) {
        runTask(new Runnable() {
                public void run() {
                	String[] s = message.split("\n");
                	for (String m : s){
                		sender.sendMessage(m);
                	}
                }
            });
    }

    void sendErrorMessage(final CommandSender sender, final Throwable e) {
        e.printStackTrace();
        sendMessage(sender, message.get("error"));
    }

    int runTask(final Runnable task) {
        return getServer().getScheduler().scheduleSyncDelayedTask(this, task);
    }

    int runTask(final Runnable task, final long delay) {
        return getServer().getScheduler().scheduleSyncDelayedTask(this, task, delay);
    }

    int runAsyncTask(final Runnable task) {
        return getServer().getScheduler().scheduleAsyncDelayedTask(this, task);
    }

    int runAsyncTask(final Runnable task, final long delay) {
        return getServer().getScheduler().scheduleAsyncDelayedTask(this, task, delay);
    }

    void cancelTask(int task) {
        getServer().getScheduler().cancelTask(task);
    }

    public JunctionClient getNewClient() throws HttpException, IOException {
    	return new JunctionClient(
                getConfig().getString("api.base"),
                getConfig().getString("api.username"),
                getConfig().getString("api.password"));
    }
    
    public void logException(Exception e) {
    	
    }
    
}
