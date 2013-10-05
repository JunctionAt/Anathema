package at.junction.anathema;

import at.junction.api.BanAPI;

import java.io.File;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Anathema extends JavaPlugin{
    AnathemaListener listener;

    private BanAPI banAPI;
    private String server, endpoint, ApiKey, banAppend;
    private ChatColor color = ChatColor.RED;
    @Override
    public void onEnable(){
        File conf = new File(getDataFolder(), "config.yml");
		if (!conf.exists()){
			getConfig().options().copyDefaults(true);
			saveConfig();
		}
        server = getConfig().getString("server");
        endpoint = getConfig().getString("endpoint");
        ApiKey = getConfig().getString("ApiKey");
        banAppend = getConfig().getString("banAppend");

        banAPI = new BanAPI(endpoint, ApiKey);
        getLogger().info("BanAPI Loaded");
        listener = new AnathemaListener(this, banAPI, banAppend);

  
        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("Enabled Anathema");
    }

    @Override
    public void onDisable(){
        getLogger().info("Disabled Anathema");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if(command.getName().equalsIgnoreCase("a")) {
            if (args.length > 0){
                if (args[0].equalsIgnoreCase("ban")){
                    if (args.length < 3){
                        sender.sendMessage(color + "Usage: /a ban <player> <reason>");
                        return true;
                    }
                    Player banned = Bukkit.getPlayer(args[1]);
                    String reason = "";
                    for (int i=2; i<args.length; i++) reason += (args[i] + " ");
                    try {
                        getLogger().info(banAPI.addBan(banned.getName(), sender.getName(), reason, server));
                        banned.kickPlayer("You have been banned.\nReason: " + reason + "\n" + banAppend);
                    } catch (Exception e){
                        getLogger().severe(e.getMessage());    
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("rban")){
                    if (args.length < 2){
                        sender.sendMessage(color + "Usage: /a rban <player>");
                        return true;
                    }
                    try {
                        ArrayList<Ban> bans = getLocalBans(args[1], "true");
                        if (bans.size() != 0) {
                            getLogger().info(banAPI.delBan(args[1], sender.getName()));
                            sender.sendMessage(color + "Ban removed");
                            return true;
                         } else {
                            sender.sendMessage(color + "Player is not banned");
                            return true;
                         }
                    } catch (Exception e){
                        getLogger().severe("Error while removing ban: " + e.getMessage());
                        sender.sendMessage(color + "Ban could not be removed - please contact tech staff");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("addnote")){
                    sender.sendMessage(color + "Usage: /a addnote <player> <note>");
                    sender.sendMessage(color + "NOT YET IMPLEMENTED"); 
                    return true;
                } else if (args[0].equalsIgnoreCase("delnote")){
                    sender.sendMessage(color + "Usage: /a delnote <player> <noteID>");
                    sender.sendMessage(color + "NOT YET IMPLEMENTED");
                    return true;
                }else if (args[0].equalsIgnoreCase("lookup")){
                    if (args.length < 4){
                        sender.sendMessage(color + "Usage: /a lookup [bans|notes] [current|removed] <player>");
                        return true;
                    }
                    try {
                        if (args[2].equalsIgnoreCase("current")){
                            if (args[1].equalsIgnoreCase("bans")){
                                sender.sendMessage(color + "id: issuer | time | reason");
                                for (Ban b : getLocalBans(args[3], "true")){
                                    sender.sendMessage(color +""+ b.id + ": " + b.issuer + " | " + b.time + " | " + b.reason);
                                }
                            } else if (args[1].equalsIgnoreCase("notes")) {
                                sender.sendMessage(color + "Not Yet Implemented");
                            } 
                        } else if (args[2].equalsIgnoreCase("removed")){
                            if (args[1].equalsIgnoreCase("bans")){
                                sender.sendMessage(color + "id: issuer | time | reason");
                                for (Ban b : getLocalBans(args[3], "false")){
                                    sender.sendMessage(color +""+ b.id + ": " + b.issuer + " | " + b.time + " | " + b.reason);
                                }
                            } else if (args[1].equalsIgnoreCase("notes")) {
                                sender.sendMessage(color + "Not Yet Implemented");
                            } 
                        }
                    } catch (Exception e){
                        sender.sendMessage(color + "Error occurred: " + e.getMessage());
                    }
                    return true;
                }

                return false;
            } else {
                return false;
            }
        }
        return false;
    }
    //Get bans, put into Map<Issuer, Reason>
    public ArrayList<Ban> getLocalBans(String player, String active) throws Exception, JSONException{
        String json;
        ArrayList<Ban> ret= new ArrayList<Ban>();

        try {
            json = banAPI.getLocalBans(player, active);
        } catch (Exception e){
            getLogger().severe("E01: An error occured while using the API" + e.getMessage());
            throw e;
        }
        try {
            JSONObject info = new JSONObject(json);
            JSONArray bans = (JSONArray)info.get("bans");
            for (int i=0; i<bans.length(); i++){
                Ban tmp = new Ban();
                JSONObject ban = bans.getJSONObject(i);
                tmp.username = ban.getString("username");
                tmp.time = ban.getString("time");
                tmp.reason = ban.getString("reason");
                tmp.issuer = ban.getString("issuer");
                tmp.id = ban.getInt("id");
                ret.add(tmp);
            }
            return ret ;
            
        } catch (JSONException e){
            getLogger().severe("E02: An error occured while decoding json");
            throw e;
        }
        



    }


}
