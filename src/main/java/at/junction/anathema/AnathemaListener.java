package at.junction.anathema;

import at.junction.api.BanAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.io.IOException;

public class AnathemaListener implements Listener {
    private Anathema plugin;
    private BanAPI banAPI;
    private String banAppend;

    public AnathemaListener(Anathema plugin, BanAPI banAPI, String banAppend) {
        this.plugin = plugin;
        this.banAPI = banAPI;
        this.banAppend = banAppend;
   }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        try {
            ArrayList<Ban> bans = plugin.getLocalBans(event.getName(), "true");
            if (bans.size() > 0){ //Player is banned
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You have been banned from this server.\nReason: " + bans.get(0).reason + "\n" + banAppend);
            }
        } catch (Exception e){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "BanAPI Caused an error. Please contact tech staff");
        }
    }
}
