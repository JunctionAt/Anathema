package at.junction.anathema;

import at.junction.api.bans.Ban;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;



import java.util.List;


public class AnathemaListener implements Listener {
    private Anathema plugin;

    public AnathemaListener(Anathema plugin) {
        this.plugin = plugin;
   }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        try {
            List<Ban> bans = plugin.banAPI.getLocalBans(event.getName(), "true");
            if (bans.size() > 0){ //Player is banned
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You have been banned from this server.\nReason: " + bans.get(0).reason + "\n" + plugin.config.BANAPPEND);
                return;
            }
        } catch (Exception exception){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "BanAPI Caused an error. Please contact tech staff");
            plugin.getLogger().severe("E01: Failed to access ban api. Message: " + exception.getMessage());
            return;
        }
        try {
            plugin.altAPI.add(event.getName(), event.getAddress().getHostAddress());
        } catch (Exception exception){
            plugin.getLogger().severe("E02: Failed to log alt information. Message: " + exception.getMessage());
        }

    }
}
