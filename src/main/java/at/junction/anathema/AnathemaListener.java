package at.junction.anathema;

import at.junction.api.BanStatus;
import at.junction.api.fields.PlayerIdentifier;
import at.junction.api.rest.BansApi;
import at.junction.api.rest.BansApi.Ban;
import at.junction.api.rest.BansApi.Note;
import at.junction.api.rest.AltApi.Alt;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;


import java.util.List;


public class AnathemaListener implements Listener {
    private Anathema plugin;

    public AnathemaListener(Anathema plugin) {
        this.plugin = plugin;
   }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        try {
            PlayerIdentifier target = new PlayerIdentifier(plugin.getServer().getPlayer(event.getName()).getUniqueId(), event.getName());
            List<Ban> bans = plugin.banAPI.getBans(new PlayerIdentifier(event.getUniqueId(), event.getName()), BanStatus.Active);
            if (bans.size() > 0){ //Player is banned
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You have been banned from this server.\nReason: " + bans.get(0).reason() + "\n" + plugin.config.BANAPPEND);
                try {
                    plugin.altAPI.ensurePlayerData(event.getAddress().getHostAddress(), target, false);
                    System.out.println("Disallowed login event: Updated alt DB");
                } catch (Exception exception){
                    plugin.getLogger().severe("E02: Failed to log alt information. Message: " + exception.getMessage());
                }
            }
        } catch (Exception exception){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "BanAPI Caused an error. Please contact tech staff");
            plugin.getLogger().severe("E01: Failed to access ban api. Message: " + exception.getMessage());
            exception.printStackTrace();
        }


    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(PlayerLoginEvent event){
        try {
            //Log successful login
            try {
                PlayerIdentifier target = new PlayerIdentifier(event.getPlayer().getUniqueId(), event.getPlayer().getName());
                plugin.altAPI.ensurePlayerData(event.getAddress().getHostAddress(), target, true);
                System.out.println("Allowed login event: Updated Alt DB");
            } catch (Exception exception){
                plugin.getLogger().severe("E02: Failed to log alt information. Message: " + exception.getMessage());
            }
            List<Note> notes = plugin.banAPI.getNotes(PlayerIdentifier.apply(event.getPlayer()), BanStatus.Active);
            if (notes.size() == 0) return;
            plugin.staffBroadcast(String.format("%s has %s notes", event.getPlayer().getName(), notes.size()));
            for (Note n : notes){
                String message = String.format("%s %s-%s", n.note(), ChatColor.DARK_PURPLE, n.issuer());
                plugin.staffBroadcast(message);
            }
        } catch (Exception e){
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
