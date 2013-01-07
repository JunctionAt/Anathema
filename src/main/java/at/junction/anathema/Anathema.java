package at.junction.anathema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.command.*;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;

import at.junction.anathema.BanApi.APIException;
import at.junction.api.*;

public class Anathema extends JavaPlugin implements Listener
{
    String server;
    JunctionAPI api;
    LookupContextStore contextStore;
    final HashMap<String,String> message = new HashMap<String,String>();

    String base;
    String username;
    String password;
    
    @Override
    public void onEnable() {
    	getConfig().options().copyDefaults(true);
    	this.saveDefaultConfig();
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
        
        base = getConfig().getString("api.base");
        username = getConfig().getString("api.username");
        password = getConfig().getString("api.password");
        
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
        a(new ActionCommandExecutor()),
        c(new ContextCommandExecutor());

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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
    	LookupResponse response = null;
    	try {
			response = BanApi.getLocalBans(api, event.getName());
		} catch (HttpException e) {
			getLogger().severe(e.getMessage());
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred when contacting the api. Please contact tech staff.");
			return;
		} catch (JSONException e) {
			getLogger().severe(e.getMessage());
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred when parsing the api response. Please contact tech staff.");
			return;
		} catch (IOException e) {
			getLogger().severe(e.getMessage());
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred when reading the response. Please contact tech staff.");
			return;
		} catch (APIException e) {
			getLogger().severe(e.getMessage());
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "An error occurred in the API server. Please contact tech staff.");
			return;
		}
    	
    	if(response==null) {
			getLogger().severe("Error occurred in the local bans lookup function, it returned null. Fix this ASAP!!");
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The lookup function returned null. Please contact tech staff.");
			return;
    	}
    	
    	ArrayList<Ban> bans = response.getBans();
    	if(bans!=null && bans.size()!=0) {
    		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, bans.get(0).reason);
    		return;
    	}
    	
    	event.allow();
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
