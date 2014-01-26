package at.junction.anathema;

import at.junction.api.bans.Ban;
import at.junction.api.bans.Note;

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
            List<Ban> bans = plugin.banAPI.getLocalBans(event.getName(), "true");
            if (bans.size() > 0){ //Player is banned
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "You have been banned from this server.\nReason: " + bans.get(0).reason + "\n" + plugin.config.BANAPPEND);
                try {
                    plugin.altAPI.add(event.getAddress().getHostAddress(), event.getName(), false);
                    System.out.println("Disallowed login event: Updated alt DB");
                } catch (Exception exception){
                    plugin.getLogger().severe("E02: Failed to log alt information. Message: " + exception.getMessage());
                }
            }
        } catch (Exception exception){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "BanAPI Caused an error. Please contact tech staff");
            plugin.getLogger().severe("E01: Failed to access ban api. Message: " + exception.getMessage());
        }


    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(PlayerLoginEvent event){
        try {
            //Log successful login
            try {
                plugin.altAPI.add(event.getAddress().getHostAddress(), event.getPlayer().getName(), true);
                System.out.println("Allowed login event: Updated Alt DB");
            } catch (Exception exception){
                plugin.getLogger().severe("E02: Failed to log alt information. Message: " + exception.getMessage());
            }
            List<Note> notes = plugin.banAPI.getLocalNotes(event.getPlayer().getName(), "true");
            if (notes.size() == 0) return;
            plugin.staffBroadcast(String.format("%s[A]%s%s has %s notes", ChatColor.GREEN, ChatColor.RESET,
                    event.getPlayer().getName(), notes.size()));
            for (Note n : notes){
                String message = String.format("%s[A]%s%s -%s", ChatColor.GREEN, ChatColor.RESET, n.note, n.issuer);
                plugin.staffBroadcast(message);
            }
        } catch (Exception e){
            plugin.getLogger().severe(e.getMessage());
        }
    }
}
